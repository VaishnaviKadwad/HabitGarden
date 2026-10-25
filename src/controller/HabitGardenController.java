package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import model.Cactus;
import model.Flower;
import model.Habit;
import model.HabitManager;
import model.Plant;
import model.Tree;
import view.GardenView;
import view.PlantView;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller for main.fxml (Member 2).
 * Connects the garden, the side panel and the habit list to the HabitManager (Member 1).
 */
public class HabitGardenController {

    // Set to false once Member 3's storage is connected (see persist / loadSavedHabits).
    private static final boolean DEMO_MODE = true;

    // ---------- model ----------
    private final HabitManager manager = new HabitManager();
    private final ObservableList<Habit> items = FXCollections.observableArrayList();
    private GardenView garden;
    private Habit selected;                 // the plant shown in the side panel (or null)
    private boolean syncingList = false;    // stops the list and garden selecting each other in a loop
    private LocalDate lastRefreshDay;

    // ---------- widgets injected from main.fxml ----------
    @FXML private StackPane gardenHolder;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> frequencyBox;
    @FXML private ComboBox<String> plantBox;
    @FXML private Label summaryLabel;
    @FXML private Label messageLabel;
    @FXML private Label detailTitle;
    @FXML private Label detailInfo;
    @FXML private ProgressBar stageProgress;
    @FXML private Label nextStageLabel;
    @FXML private Button waterButton;
    @FXML private Button removeButton;
    @FXML private ListView<Habit> habitList;

    // ======================================================================
    // Start-up
    // ======================================================================

    /** Called automatically by JavaFX after main.fxml is loaded. */
    @FXML
    public void initialize() {
        frequencyBox.getItems().addAll("DAILY", "WEEKLY");
        frequencyBox.getSelectionModel().selectFirst();
        plantBox.getItems().addAll("FLOWER", "TREE", "CACTUS");
        plantBox.getSelectionModel().selectFirst();
        nameField.setOnAction(e -> handleAddHabit());   // Enter also plants the habit

        // the garden scene: click = select, double-click = water, click on grass = deselect
        garden = new GardenView(this::select, this::water, () -> select(null));
        gardenHolder.getChildren().add(garden);

        // the list on the right
        habitList.setItems(items);
        habitList.setCellFactory(list -> new HabitCell());
        habitList.getSelectionModel().selectedItemProperty().addListener((obs, oldHabit, newHabit) -> {
            if (!syncingList && newHabit != null) {
                select(newHabit);
            }
        });

        loadSavedHabits();
        if (DEMO_MODE && manager.getHabits().isEmpty()) {
            addDemoData();
        }
        refreshUI();
        if (!items.isEmpty()) {
            select(items.get(0));
        }

        // every minute: update the sky, and redraw if the date has changed (midnight)
        Timeline clock = new Timeline(new KeyFrame(Duration.minutes(1), e -> onClockTick()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    /** Member 3 hook: load habits from the CSV file when the app starts. */
    private void loadSavedHabits() {
        // TODO (Member 3 / integration):
        // try {
        //     for (Habit h : storage.loadHabits()) {
        //         try {
        //             manager.addHabit(h);              // one bad row is skipped,
        //         } catch (IllegalArgumentException e) { // the rest still load
        //             System.err.println("Skipped habit: " + e.getMessage());
        //         }
        //     }
        // } catch (Exception e) {
        //     showError("Could not load habits", e.getMessage());
        // }
    }

    /** Member 3 hook: save all habits to the CSV file. Called after every change. */
    private void persist() {
        // TODO (Member 3 / integration):
        // try {
        //     storage.saveHabits(manager.getHabits());
        // } catch (Exception e) {
        //     showError("Could not save habits", e.getMessage());
        // }
    }

    /** Sample habits so every growth stage and the wilted state can be seen without a CSV file. */
    private void addDemoData() {
        LocalDate today = LocalDate.now();
        manager.addHabit(new Habit("Drink Water", "DAILY", 6, today.minusDays(1), "FLOWER"));
        manager.addHabit(new Habit("Study 1 Hour", "DAILY", 5, today.minusDays(1), "TREE"));
        manager.addHabit(new Habit("Gym", "DAILY", 7, today.minusDays(4), "CACTUS"));
        manager.addHabit(new Habit("Read a Book", "WEEKLY", 0, null, "FLOWER"));
        manager.addHabit(new Habit("Sleep by 11", "DAILY", 10, today, "TREE"));
        manager.addHabit(new Habit("Meditate", "WEEKLY", 4, today.minusDays(3), "CACTUS"));
    }

    // ======================================================================
    // Actions
    // ======================================================================

    @FXML
    private void handleAddHabit() {
        try {
            String name = nameField.getText();
            String frequency = frequencyBox.getValue();
            String plant = plantBox.getValue();
            if (frequency == null || plant == null) {
                throw new IllegalArgumentException("Please choose a frequency and a plant.");
            }

            Habit habit = manager.addHabit(name, frequency, plant);   // Member 1's code validates the input

            nameField.clear();
            nameField.requestFocus();
            persist();
            selected = habit;
            refreshUI();
            garden.sproutIn(habit);
            showMessage("Planted a " + plant.toLowerCase() + " seed for \"" + habit.getHabitName() + "\".");
        } catch (IllegalArgumentException e) {
            showError("Cannot plant this habit", e.getMessage());
        }
    }

    @FXML
    private void handleDone() {
        water(selected);
    }

    @FXML
    private void handleDelete() {
        if (selected == null) {
            return;
        }
        Habit habit = selected;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove \"" + habit.getHabitName() + "\" and its plant from the garden?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Remove habit");
        confirm.setHeaderText("Are you sure?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            manager.deleteHabit(habit);
            selected = null;
            persist();
            refreshUI();
            showMessage("Removed \"" + habit.getHabitName() + "\".");
        }
    }

    /** Marks the habit as done. Used by the button and by double-clicking a plant. */
    private void water(Habit habit) {
        if (habit == null) {
            return;
        }
        if (!habit.isDue()) {
            select(habit);
            showMessage("\"" + habit.getHabitName() + "\" is already watered " + periodWord(habit) + ".");
            return;
        }
        int stageBefore = habit.getPlant().getGrowthStage();
        boolean wasWilted = habit.getPlant().isWilted();
        try {
            manager.completeHabit(habit);
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError("Cannot water this plant", e.getMessage());
            return;
        }
        persist();
        selected = habit;
        refreshUI();

        boolean grew = habit.getPlant().getGrowthStage() > stageBefore;
        garden.celebrate(habit, grew);

        String plantStage = PlantView.create(habit).getStageName();
        if (wasWilted) {
            showMessage("You saved \"" + habit.getHabitName() + "\"! Its streak starts again at 1.");
        } else if (grew) {
            showMessage("\"" + habit.getHabitName() + "\" grew! It is now: " + plantStage + ".");
        } else {
            showMessage("Watered \"" + habit.getHabitName() + "\". Streak: " + habit.getStreak() + ".");
        }
    }

    /** Selects a habit in the garden, the list and the details panel (null = nothing). */
    private void select(Habit habit) {
        selected = habit;
        syncListSelection();
        garden.setSelected(habit);
        updateDetails();
    }

    private void onClockTick() {
        garden.updateSky();
        if (!LocalDate.now().equals(lastRefreshDay)) {
            refreshUI();                      // a new day: some habits are due again
        }
    }

    // ======================================================================
    // Refreshing the screen
    // ======================================================================

    /** Rebuilds the garden, the list, the details panel and the summary from the model. */
    private void refreshUI() {
        manager.refreshAll();                 // updates wilted / growth stage by today's date
        lastRefreshDay = LocalDate.now();
        if (selected != null && !manager.getHabits().contains(selected)) {
            selected = null;
        }
        syncingList = true;
        items.setAll(manager.getHabits());
        syncingList = false;
        habitList.refresh();
        syncListSelection();
        garden.setHabits(manager.getHabits(), selected);
        updateDetails();
        updateSummary();
    }

    private void syncListSelection() {
        syncingList = true;
        if (selected == null) {
            habitList.getSelectionModel().clearSelection();
        } else {
            habitList.getSelectionModel().select(selected);
        }
        syncingList = false;
    }

    private void updateSummary() {
        long due = manager.getHabits().stream().filter(Habit::isDue).count();
        long wilted = manager.getHabits().stream().filter(h -> h.getPlant().isWilted()).count();
        summaryLabel.setText(manager.getHabits().size() + " plants  |  "
                + due + " need water  |  " + wilted + " wilting");
    }

    /** Fills the "selected plant" card on the right. */
    private void updateDetails() {
        boolean hasSelection = selected != null;
        removeButton.setDisable(!hasSelection);
        stageProgress.setVisible(hasSelection);

        if (!hasSelection) {
            detailTitle.setText("Pick a plant");
            detailInfo.setText("Click a plant in the garden (or a habit in the list) to see how it is growing. "
                    + "Double-click a plant to water it.");
            nextStageLabel.setText("");
            waterButton.setDisable(true);
            waterButton.setText("Water it (Done)");
            return;
        }

        PlantView look = PlantView.create(selected);
        detailTitle.setText(selected.getHabitName());
        detailInfo.setText(look.getTypeLabel() + "  -  " + look.getStageName() + "\n"
                + selected.getFrequency().toLowerCase() + "  -  " + streakText(selected) + "\n"
                + "Status: " + statusText(selected));

        waterButton.setDisable(!selected.isDue());
        waterButton.setText(selected.isDue() ? "Water it (Done)" : "Done " + periodWord(selected));
        updateGrowthProgress(selected, look);
    }

    /** Shows how close the plant is to its next stage, using the plant's own grow() rules. */
    private void updateGrowthProgress(Habit habit, PlantView look) {
        if (habit.getPlant().isWilted()) {
            stageProgress.setProgress(0);
            nextStageLabel.setText("This plant is wilting. Water it to save it - the streak will restart at 1.");
            return;
        }
        Plant probe = newPlantOfType(habit.getPlantType());   // a spare plant to test streak values on
        int streak = habit.getStreak();
        probe.grow(streak);
        int stage = probe.getGrowthStage();
        if (stage == Plant.FLOWER) {
            stageProgress.setProgress(1.0);
            nextStageLabel.setText("Fully grown! Keep the streak going.");
            return;
        }

        int stageStart = streak;                  // lowest streak that still gives this stage
        while (stageStart > 0) {
            probe.grow(stageStart - 1);
            if (probe.getGrowthStage() != stage) {
                break;
            }
            stageStart--;
        }
        int nextStageAt = streak + 1;             // first streak that gives the next stage
        probe.grow(nextStageAt);
        while (probe.getGrowthStage() == stage && nextStageAt < 1000) {
            nextStageAt++;
            probe.grow(nextStageAt);
        }

        int left = nextStageAt - streak;
        stageProgress.setProgress((double) (streak - stageStart) / (nextStageAt - stageStart));
        String unit = "DAILY".equals(habit.getFrequency())
                ? (left == 1 ? "day" : "days")
                : (left == 1 ? "week" : "weeks");
        nextStageLabel.setText(left + " more " + unit + " until: " + look.getStageName(probe.getGrowthStage()));
    }

    private Plant newPlantOfType(String plantType) {
        switch (plantType) {
            case "TREE":
                return new Tree();
            case "CACTUS":
                return new Cactus();
            default:
                return new Flower();
        }
    }

    // ---------- small text helpers ----------

    private String streakText(Habit habit) {
        if (habit.isMissed()) {
            return "streak broken (was " + habit.getStreak() + ")";
        }
        return "streak " + habit.getStreak();
    }

    private String statusText(Habit habit) {
        if (habit.getPlant().isWilted()) {
            return "wilting - needs water";
        }
        if (habit.isDue()) {
            return "needs water " + periodWord(habit);
        }
        return "watered";
    }

    private String statusStyle(Habit habit) {
        if (habit.getPlant().isWilted()) {
            return "dot-wilted";
        }
        return habit.isDue() ? "dot-due" : "dot-done";
    }

    /** "today" for a daily habit, "this week" for a weekly one. */
    private String periodWord(Habit habit) {
        return "DAILY".equals(habit.getFrequency()) ? "today" : "this week";
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ======================================================================
    // How each row of the habit list is drawn
    // ======================================================================

    private class HabitCell extends ListCell<Habit> {
        private final Circle dot = new Circle(6);
        private final Label name = new Label();
        private final Label info = new Label();
        private final HBox row = new HBox(10, dot, new VBox(2, name, info));

        HabitCell() {
            row.setAlignment(Pos.CENTER_LEFT);
            setPrefWidth(0);                       // fit the list width: no sideways scrolling
            name.getStyleClass().add("cell-name");
            info.getStyleClass().add("cell-info");
        }

        @Override
        protected void updateItem(Habit habit, boolean empty) {
            super.updateItem(habit, empty);
            setText(null);
            if (empty || habit == null) {
                setGraphic(null);
                return;
            }
            PlantView look = PlantView.create(habit);
            name.setText(habit.getHabitName());
            String streak = habit.isMissed() ? "streak broken" : "streak " + habit.getStreak();
            info.setText(look.getTypeLabel() + " - " + look.getStageName() + " - " + streak);
            dot.getStyleClass().setAll("status-dot", statusStyle(habit));
            setGraphic(row);
        }
    }
}
