package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One habit the user wants to build.
 * (Encapsulation: all fields are private; access is through getters/setters.
 *  Composition: every Habit owns a Plant.)
 */
public class Habit {

    private String habitName;
    private String frequency;            // "DAILY" or "WEEKLY"
    private int streak;
    private LocalDate lastCompletedDate; // null if never completed
    private String plantType;            // "FLOWER", "TREE" or "CACTUS"
    private Plant plant;

    /** Constructor for a brand-new habit. */
    public Habit(String habitName, String frequency, String plantType) {
        setHabitName(habitName);
        setFrequency(frequency);
        setPlantType(plantType);
        this.streak = 0;
        this.lastCompletedDate = null;
        refreshPlant();
    }

    /** Constructor used when loading a saved habit from the CSV file. */
    public Habit(String habitName, String frequency, int streak,
                 LocalDate lastCompletedDate, String plantType) {
        setHabitName(habitName);
        setFrequency(frequency);
        setPlantType(plantType);
        setStreak(streak);
        this.lastCompletedDate = lastCompletedDate;
        refreshPlant();
    }

    // ------------------------------------------------------------------
    // Core logic
    // ------------------------------------------------------------------

    /** Days between the last completion and today (or -1 if never done). */
    private long daysSinceLastDone() {
        if (lastCompletedDate == null) {
            return -1;
        }
        return ChronoUnit.DAYS.between(lastCompletedDate, LocalDate.now());
    }

    /** Can the user complete this habit right now? */
    public boolean isDue() {
        long days = daysSinceLastDone();
        if (days == -1) return true;               // never done yet
        if (frequency.equals("DAILY")) return days >= 1;
        return days >= 7;                          // WEEKLY
    }

    /** Has the user missed their window, so the streak is broken? */
    public boolean isMissed() {
        long days = daysSinceLastDone();
        if (days == -1) return false;              // never done: nothing to miss yet
        if (frequency.equals("DAILY")) return days > 1;
        return days > 7;                           // WEEKLY
    }

    /** Mark the habit as completed today. */
    public void markDone() {
        if (!isDue()) {
            throw new IllegalStateException(
                "'" + habitName + "' is already completed for this period.");
        }
        if (isMissed()) {
            streak = 1;     // streak was broken, start again
        } else {
            streak++;
        }
        lastCompletedDate = LocalDate.now();
        refreshPlant();
    }

    /** Recalculate the plant's growth stage and wilted state. */
    public void refreshPlant() {
        plant.setWilted(isMissed());
        plant.grow(streak);
    }

    /** Creates the right Plant subclass for a plant type. */
    private static Plant createPlant(String plantType) {
        switch (plantType) {
            case "FLOWER": return new Flower();
            case "TREE":   return new Tree();
            case "CACTUS": return new Cactus();
            default:
                throw new IllegalArgumentException(
                    "Plant type must be FLOWER, TREE or CACTUS.");
        }
    }

    // ------------------------------------------------------------------
    // Getters and setters (with validation)
    // ------------------------------------------------------------------

    public String getHabitName() { return habitName; }

    public void setHabitName(String habitName) {
        if (habitName == null || habitName.trim().isEmpty()) {
            throw new IllegalArgumentException("Habit name cannot be empty.");
        }
        if (habitName.contains(",")) {
            // commas would break the CSV file
            throw new IllegalArgumentException("Habit name cannot contain commas.");
        }
        this.habitName = habitName.trim();
    }

    public String getFrequency() { return frequency; }

    public void setFrequency(String frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException("Frequency cannot be empty.");
        }
        String f = frequency.trim().toUpperCase();
        if (!f.equals("DAILY") && !f.equals("WEEKLY")) {
            throw new IllegalArgumentException("Frequency must be DAILY or WEEKLY.");
        }
        this.frequency = f;
    }

    public int getStreak() { return streak; }

    public void setStreak(int streak) {
        if (streak < 0) {
            throw new IllegalArgumentException("Streak cannot be negative.");
        }
        this.streak = streak;
    }

    public LocalDate getLastCompletedDate() { return lastCompletedDate; }

    public String getPlantType() { return plantType; }

    public void setPlantType(String plantType) {
        if (plantType == null) {
            throw new IllegalArgumentException("Plant type cannot be empty.");
        }
        String p = plantType.trim().toUpperCase();
        this.plant = createPlant(p);   // throws if p is not valid
        this.plantType = p;
        if (frequency != null) {
            refreshPlant();
        }
    }

    public Plant getPlant() { return plant; }

    @Override
    public String toString() {
        return habitName + " (" + frequency + ", streak " + streak + ")";
    }
}
