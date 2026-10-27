package storage;

import model.Habit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Saves and loads habits in a CSV file.
 *
 * File format (one habit per line):
 *   habitName,frequency,streak,lastCompletedDate,plantType
 *   Drink Water,DAILY,3,2026-10-22,FLOWER
 *
 * The plant's growth stage and wilted state are NOT stored: they are
 * recalculated from the streak and the date when a Habit is created.
 */
public class CsvHabitStorage implements Storable {

    public static final String HEADER = "habitName,frequency,streak,lastCompletedDate,plantType";

    private final Path filePath;
    private final List<String> loadWarnings = new ArrayList<>();

    public CsvHabitStorage(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    // ======================================================================
    // Saving
    // ======================================================================

    @Override
    public void saveHabits(List<Habit> habits) throws IOException {
        if (habits == null) {
            throw new IllegalArgumentException("Habit list cannot be null.");
        }

        Path folder = filePath.toAbsolutePath().getParent();
        Path tempFile = folder.resolve(filePath.getFileName() + ".tmp");

        try {
            Files.createDirectories(folder);

            // Write to a temporary file first, so a crash half-way through
            // can never destroy the existing habits.csv.
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (Habit habit : habits) {
                    writer.write(toCsvLine(habit));
                    writer.newLine();
                }
            }
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // nothing more we can do
            }
            throw new IOException("Could not save habits to " + filePath + ": " + e.getMessage(), e);
        }
    }

    private String toCsvLine(Habit habit) {
        LocalDate date = habit.getLastCompletedDate();
        return habit.getHabitName() + ","
                + habit.getFrequency() + ","
                + habit.getStreak() + ","
                + (date == null ? "" : date.toString()) + ","
                + habit.getPlantType();
    }

    // ======================================================================
    // Loading
    // ======================================================================

    @Override
    public List<Habit> loadHabits() throws IOException {
        loadWarnings.clear();
        List<Habit> habits = new ArrayList<>();
        Set<String> namesSeen = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;                                   // blank line
                }
                if (lineNumber == 1 && line.trim().toLowerCase().startsWith("habitname")) {
                    continue;                                   // header row
                }

                try {
                    Habit habit = parseLine(line);
                    if (!namesSeen.add(habit.getHabitName().toLowerCase())) {
                        loadWarnings.add("Line " + lineNumber + " skipped: duplicate habit '"
                                + habit.getHabitName() + "'.");
                        continue;
                    }
                    habits.add(habit);
                } catch (IllegalArgumentException e) {
                    // one bad row must not stop the rest of the file from loading
                    loadWarnings.add("Line " + lineNumber + " skipped: " + e.getMessage());
                }
            }

        } catch (FileNotFoundException e) {
            // First run: there is no habits.csv yet. That is normal, not an error.
            return new ArrayList<>();
        } catch (IOException e) {
            throw new IOException("Could not read habits from " + filePath + ": " + e.getMessage(), e);
        }

        return habits;
    }

    /** Turns one CSV line into a Habit. Throws IllegalArgumentException if the line is invalid. */
    private Habit parseLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("expected 5 columns but found " + parts.length + ".");
        }

        String name = parts[0].trim();
        String frequency = parts[1].trim();
        String plantType = parts[4].trim();

        int streak;
        try {
            streak = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("streak '" + parts[2].trim() + "' is not a whole number.");
        }

        LocalDate lastDone = null;
        String dateText = parts[3].trim();
        if (!dateText.isEmpty()) {
            try {
                lastDone = LocalDate.parse(dateText);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("date '" + dateText + "' must look like 2026-10-23.");
            }
            if (lastDone.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("date '" + dateText + "' is in the future.");
            }
        } else if (streak > 0) {
            throw new IllegalArgumentException("streak is " + streak + " but there is no completion date.");
        }

        // Habit's own validation checks name, frequency, plant type and negative streaks
        return new Habit(name, frequency, streak, lastDone, plantType);
    }

    /** Problems found during the last loadHabits() call (bad lines that were skipped). */
    public List<String> getLoadWarnings() {
        return new ArrayList<>(loadWarnings);
    }

    public Path getFilePath() {
        return filePath;
    }
}
