# HabitGarden Final Report: Outline

1. **Title page**: project title, course (Activity Component_OOP_CS34), team members and roles
2. **Introduction**: the problem (checklists give little motivation) and the idea (habits as growing plants)
3. **Features implemented**: add / complete / delete, streaks, 4 growth stages, wilted state, Garden dashboard, CSV persistence, error handling
4. **System design**
   - Package structure (`model`, `controller`, `storage`, `resources`)
   - Class diagram (from Member 1)
   - Screen descriptions with screenshots (My Habits tab, Garden tab, error alert, wilted plant)
5. **OOP concepts used** (table: concept / where in code / why)
   - Encapsulation: private fields and validated setters in `Habit`
   - Inheritance: `Flower`, `Tree`, `Cactus` extend `Plant`
   - Polymorphism: overridden `grow()` and `getPlantImage()`
   - Abstraction: abstract `Plant`
   - Interface: `Storable` implemented by `CsvHabitStorage`
   - Composition: `HabitManager` has Habits, `Habit` has a Plant
   - Exception handling: IllegalArgumentException / IllegalStateException in the model, IOException and FileNotFoundException in storage, alerts in the UI
6. **Data storage**: CSV format, example rows, how loading handles missing or invalid data, the temp-file save
7. **Testing**: summary of `TEST_CHECKLIST.md` results (total / passed / failed), the console tests, bugs found and fixed
8. **Challenges and solutions**: e.g. keeping the three parts in sync, date logic for wilting, resource paths in JavaFX
9. **Limitations and future work**: no reminders, no cloud sync, no completion history, more plant types
10. **Conclusion**
11. **Appendix**: screenshots, source file list, demo script
