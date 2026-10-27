import model.Habit;
import storage.CsvHabitStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Console tests for the storage layer (Member 3). Run before integrating with the UI. */
public class StorageTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String testName, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + testName);
        } else {
            failed++;
            System.out.println("FAIL  " + testName);
        }
    }

    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("habitgarden-test");
        LocalDate today = LocalDate.now();

        // T1: loading when the file does not exist gives an empty list (first run)
        CsvHabitStorage missing = new CsvHabitStorage(dir.resolve("nothing.csv").toString());
        check("T1 missing file -> empty list, no exception", missing.loadHabits().isEmpty());

        // T2: save then load gives the same data
        Path file = dir.resolve("data").resolve("habits.csv");     // folder 'data' does not exist yet
        CsvHabitStorage storage = new CsvHabitStorage(file.toString());
        List<Habit> original = new ArrayList<>();
        original.add(new Habit("Drink Water", "DAILY", 3, today.minusDays(1), "FLOWER"));
        original.add(new Habit("Read", "WEEKLY", 0, null, "CACTUS"));
        original.add(new Habit("Study 1 Hour", "DAILY", 8, today, "TREE"));
        storage.saveHabits(original);
        check("T2a save creates the folder and file", Files.exists(file));

        List<Habit> loaded = storage.loadHabits();
        boolean same = loaded.size() == 3
                && loaded.get(0).getHabitName().equals("Drink Water")
                && loaded.get(0).getStreak() == 3
                && today.minusDays(1).equals(loaded.get(0).getLastCompletedDate())
                && loaded.get(0).getPlantType().equals("FLOWER")
                && loaded.get(1).getLastCompletedDate() == null
                && loaded.get(1).getFrequency().equals("WEEKLY")
                && loaded.get(2).getStreak() == 8;
        check("T2b save then load returns identical habits", same);
        check("T2c no warnings on a clean file", storage.getLoadWarnings().isEmpty());
        check("T2d growth stage rebuilt from streak (streak 8 TREE = BUD)",
                loaded.get(2).getPlant().getStageName().equals("BUD"));
        check("T2e temp file removed after save", !Files.exists(dir.resolve("data").resolve("habits.csv.tmp")));

        // T3: saving an empty list gives a header-only file that loads as empty
        storage.saveHabits(new ArrayList<>());
        check("T3 empty list round trip", storage.loadHabits().isEmpty());

        // T4: bad lines are skipped, good lines still load, warnings are reported
        String messy = String.join("\n",
                "habitName,frequency,streak,lastCompletedDate,plantType",
                "Good Habit,DAILY,2," + today.minusDays(1) + ",FLOWER",
                "Missing columns,DAILY",
                "Bad streak,DAILY,abc," + today + ",TREE",
                "Bad date,DAILY,1,23-10-2026,TREE",
                "Future date,DAILY,1," + today.plusDays(5) + ",TREE",
                "Bad frequency,MONTHLY,1," + today + ",TREE",
                "Bad plant,DAILY,1," + today + ",ROSE",
                "Negative,DAILY,-4," + today + ",TREE",
                "Streak no date,DAILY,3,,TREE",
                "good habit,WEEKLY,1," + today + ",CACTUS",
                "",
                "Second Good,WEEKLY,4," + today.minusDays(3) + ",CACTUS");
        Path messyFile = dir.resolve("messy.csv");
        Files.write(messyFile, messy.getBytes(StandardCharsets.UTF_8));
        CsvHabitStorage messyStorage = new CsvHabitStorage(messyFile.toString());
        List<Habit> messyLoaded = messyStorage.loadHabits();
        check("T4a only the 2 valid habits are loaded", messyLoaded.size() == 2);
        check("T4b 9 warnings reported (8 bad rows + 1 duplicate)", messyStorage.getLoadWarnings().size() == 9);
        messyStorage.getLoadWarnings().forEach(w -> System.out.println("      warning: " + w));

        // T5: an empty file loads as an empty list
        Path emptyFile = dir.resolve("empty.csv");
        Files.write(emptyFile, new byte[0]);
        check("T5 empty file -> empty list", new CsvHabitStorage(emptyFile.toString()).loadHabits().isEmpty());

        // T6: saving to an impossible location throws IOException with a clear message
        Path blocker = dir.resolve("iamafile");
        Files.write(blocker, "x".getBytes(StandardCharsets.UTF_8));
        CsvHabitStorage impossible = new CsvHabitStorage(blocker.resolve("habits.csv").toString());
        try {
            impossible.saveHabits(original);
            check("T6 impossible path should throw IOException", false);
        } catch (IOException e) {
            check("T6 impossible path -> IOException with message", e.getMessage().contains("Could not save"));
        }

        // T7: a wilted habit is still wilted after loading (date-based, not stored)
        List<Habit> old = new ArrayList<>();
        old.add(new Habit("Gym", "DAILY", 5, today.minusDays(4), "CACTUS"));
        storage.saveHabits(old);
        check("T7 wilted state recalculated on load", storage.loadHabits().get(0).getPlant().isWilted());

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }
}
