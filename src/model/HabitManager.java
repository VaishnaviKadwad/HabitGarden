package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the list of habits and provides add / delete / complete operations.
 * (Composition: a HabitManager has many Habits.)
 */
public class HabitManager {

    private final List<Habit> habits = new ArrayList<>();

    /** Create and add a new habit (used by the Add Habit form). */
    public Habit addHabit(String name, String frequency, String plantType) {
        Habit habit = new Habit(name, frequency, plantType);   // validates input
        addHabit(habit);
        return habit;
    }

    /** Add an existing Habit object (used by the CSV loader). */
    public void addHabit(Habit habit) {
        if (habit == null) {
            throw new IllegalArgumentException("Habit cannot be null.");
        }
        if (findByName(habit.getHabitName()) != null) {
            throw new IllegalArgumentException(
                "A habit named '" + habit.getHabitName() + "' already exists.");
        }
        habits.add(habit);
    }

    /** Delete a habit by name. Returns true if something was removed. */
    public boolean deleteHabit(String name) {
        Habit habit = findByName(name);
        return habit != null && habits.remove(habit);
    }

    /** Delete a specific habit object. */
    public boolean deleteHabit(Habit habit) {
        return habits.remove(habit);
    }

    /** Mark a habit as done by name. */
    public void completeHabit(String name) {
        Habit habit = findByName(name);
        if (habit == null) {
            throw new IllegalArgumentException("No habit named '" + name + "'.");
        }
        habit.markDone();
    }

    /** Mark a specific habit object as done. */
    public void completeHabit(Habit habit) {
        if (habit == null) {
            throw new IllegalArgumentException("Habit cannot be null.");
        }
        habit.markDone();
    }

    /** Refresh every plant (call after loading from file, or on app start). */
    public void refreshAll() {
        for (Habit habit : habits) {
            habit.refreshPlant();
        }
    }

    /** Read-only view of the habits. */
    public List<Habit> getHabits() {
        return Collections.unmodifiableList(habits);
    }

    public Habit findByName(String name) {
        if (name == null) return null;
        for (Habit habit : habits) {
            if (habit.getHabitName().equalsIgnoreCase(name.trim())) {
                return habit;
            }
        }
        return null;
    }
}
