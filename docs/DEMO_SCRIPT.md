# HabitGarden Demo Script (about 5 minutes)

Before the demo: delete `data/habits.csv` for a clean start, OR copy the sample `habits.csv` for a fuller garden. Close other windows. Practise once.

| Time | Who | Do | Say |
|------|-----|----|-----|
| 0:00 | Member 3 | Show the app title screen | "HabitGarden turns habits into plants. Complete a habit and the plant grows; miss it and it wilts." |
| 0:30 | Member 2 | Add "Drink Water" (DAILY, FLOWER) | "The UI is JavaFX with FXML and CSS. The controller sends this to HabitManager." |
| 1:00 | Member 2 | Click Add with an empty name | "Invalid input shows a friendly alert instead of crashing." |
| 1:20 | Member 2 | Click Done, show streak 1 and the Garden tab | "The streak went up and the plant is now a sprout." |
| 2:00 | Member 1 | Show a streak of 6 (sample data) | "Each plant overrides grow() differently: a flower is fully grown at 6, a tree at 10, a cactus at 8. That is polymorphism." |
| 2:30 | Member 1 | Show the wilted cactus | "If a daily habit is missed for more than a day, the plant wilts and the streak will reset to 1 on the next completion." |
| 3:00 | Member 3 | Close and reopen the app | "Data is saved to a CSV file through the Storable interface and loaded when the app starts." |
| 3:30 | Member 3 | Open `habits.csv` in Notepad | "Same file, five columns. Growth stage is not saved; it is recalculated." |
| 4:00 | Member 3 | (optional) break a CSV line, reopen | "Bad lines are skipped with a warning, so one mistake never loses all the data." |
| 4:30 | Member 1 | Show the class diagram | "Plant is abstract with three subclasses; HabitManager has many Habits; each Habit owns one Plant." |

## Likely questions and who answers
- OOP concepts (encapsulation, inheritance, polymorphism, abstraction, interface, composition): Member 1
- How the screen talks to the model (controller, FXML, ObservableList): Member 2
- Why CSV, why a temp file when saving, what happens when the file is missing or corrupt: Member 3
