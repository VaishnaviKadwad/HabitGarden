# HabitGarden Test Checklist

Tester: ____________   Date: ____________   Build / branch: ____________

Result column: PASS / FAIL (write the bug ID if it fails, e.g. FAIL B-03).

TIP: to simulate days passing, close the app, edit the dates in `data/habits.csv`, then reopen.

## A. Add habit
| ID | Steps | Expected | Result |
|----|-------|----------|--------|
| A1 | Type "Drink Water", DAILY, FLOWER, click Add | Habit appears in table and as a card in Garden; message label confirms | |
| A2 | Add habit by pressing Enter in the name box | Same as A1 | |
| A3 | Click Add with an empty name | Error alert "Habit name cannot be empty." | |
| A4 | Add a name made only of spaces | Same error alert | |
| A5 | Add a name containing a comma ("Eat, sleep") | Error alert about commas | |
| A6 | Add "drink water" when "Drink Water" exists | Error alert about duplicate | |
| A7 | Add a WEEKLY habit and a TREE and a CACTUS | All three appear with correct plant type | |
| A8 | Add a very long name (60+ characters) | App does not crash; text is readable or wraps | |

## B. Complete habit
| ID | Steps | Expected | Result |
|----|-------|----------|--------|
| B1 | Click Done on a new habit | Streak becomes 1; plant becomes SPROUT; button greys out | |
| B2 | Try to complete the same habit again today | Done button is disabled (no double count) | |
| B3 | Set a DAILY habit's date to yesterday in the CSV, reopen, click Done | Streak increases by 1 | |
| B4 | Set date to 4 days ago, reopen | Status "Wilted - needs care", wilted image, amber card | |
| B5 | Click Done on the wilted habit | Streak resets to 1; plant no longer wilted | |
| B6 | WEEKLY habit completed, then check after 3 days (edit CSV) | Done stays disabled (not yet due) | |
| B7 | WEEKLY habit with date 8+ days ago | Wilted; Done enabled; streak resets to 1 | |

## C. Plant growth (SPROUT/BUD/FLOWER at streak: Flower 1/3/6, Tree 2/5/10, Cactus 2/4/8)
| ID | Steps | Expected | Result |
|----|-------|----------|--------|
| C1 | FLOWER with streak 0 / 1 / 3 / 6 (edit CSV) | SEED / SPROUT / BUD / FLOWER | |
| C2 | TREE with streak 0 / 2 / 5 / 10 | SEED / SPROUT / BUD / FLOWER | |
| C3 | CACTUS with streak 0 / 2 / 4 / 8 | SEED / SPROUT / BUD / FLOWER | |
| C4 | Each wilted image exists for each plant type | No text placeholder shown instead of an image | |

## D. Delete
| ID | Steps | Expected | Result |
|----|-------|----------|--------|
| D1 | Click Delete, choose No | Nothing changes | |
| D2 | Click Delete, choose Yes | Habit disappears from table and Garden; CSV updated | |
| D3 | Delete the last remaining habit | Garden shows "empty" message; table shows placeholder | |

## E. Persistence (save / load)
| ID | Steps | Expected | Result |
|----|-------|----------|--------|
| E1 | Add a habit, close app, reopen | Habit still there with same streak and plant | |
| E2 | Complete a habit, close app, reopen | Streak and last-done date preserved | |
| E3 | Delete a habit, close app, reopen | Habit stays deleted | |
| E4 | Delete `data/habits.csv`, start app | App opens with an empty list, no crash | |
| E5 | Delete the whole `data` folder, add a habit | Folder and file are recreated | |
| E6 | Open `habits.csv` in Notepad: check header and columns | `habitName,frequency,streak,lastCompletedDate,plantType` | |

## F. Error handling
| ID | Steps | Expected | Result |
|----|-------|----------|--------|
| F1 | Put "abc" as a streak in the CSV, reopen | App opens; warning alert lists the skipped line; other habits load | |
| F2 | Put a wrong date format (23-10-2026) | Same: line skipped with a clear warning | |
| F3 | Put a future date | Line skipped with warning | |
| F4 | Remove a comma from a line | Line skipped ("expected 5 columns") | |
| F5 | Empty CSV file | App opens with no habits | |
| F6 | Make `habits.csv` read-only, then add a habit | Error alert "Could not save habits..."; app keeps running | |

## G. UI and general
| ID | Steps | Expected | Result |
|----|-------|----------|--------|
| G1 | Start the app from a clean checkout | Opens with no errors in the console | |
| G2 | Resize and maximise the window | Layout adjusts, nothing is cut off | |
| G3 | Switch between "My Habits" and "Garden" tabs | Both always show the same habits | |
| G4 | Add 12+ habits | Garden scrolls; table scrolls | |
| G5 | Summary label ("X habits | Y due") | Numbers match what is on screen | |

## Submission checklist (from the assignment)
- [ ] The application opens without errors
- [ ] A new habit can be added
- [ ] A habit can be marked as done
- [ ] The streak updates correctly
- [ ] The plant changes growth stage
- [ ] Missed habits show a wilted state
- [ ] Data is saved after closing the app
- [ ] Data loads correctly after reopening
- [ ] Invalid input shows a proper error message
- [ ] Every team member can explain the OOP concepts used
