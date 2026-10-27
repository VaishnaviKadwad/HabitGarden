package storage;

import model.Habit;

import java.io.IOException;
import java.util.List;

/**
 * Anything that can save and load habits (Interface).
 * HabitGarden uses a CSV file today, but a database or JSON class could
 * implement the same two methods later without changing the UI.
 */
public interface Storable {

    /** Saves all habits, replacing whatever was stored before. */
    void saveHabits(List<Habit> habits) throws IOException;

    /** Loads all habits. Returns an empty list if nothing has been saved yet. */
    List<Habit> loadHabits() throws IOException;
}
