package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Habit;
import model.HabitManager;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for main.fxml (Member 2).
 * Connects the buttons and lists on screen to the HabitManager (Member 1).
 */
public class HabitGardenController {

    // Set to false once Member 3's storage is connected (see persist / loadSavedHabits).
    private static final boolean DEMO_MODE = true;

    // ---------- model ----------
    private final HabitManager manager = new HabitManager();
    private final ObservableList<Habit> habitList = FXCollections.observableArrayList();
    private final Map<String, Image> imageCache = new HashMap<>();

    // ---------- widgets injected from main.fxml ----------
    @FXML private TextField nameField;
    @FXML private ComboBox<String> frequencyBox;
    @FXML private ComboBox<String> plantBox;
    @FXML private Label summaryLabel;
    @FXML private Label messageLabel;
    @FXML private TableView<Habit> habitTable;
    @FXML private TableColumn<Habit, String> nameCol;
    @FXML private TableColumn<Habit, String> frequencyCol;
    @FXML private TableColumn<Habit, String> plantCol;
    @FXML private TableColumn<Habit, String> stageCol;
    @FXML private TableColumn<Habit, String> streakCol;
    @FXML private TableColumn<Habit, String> statusCol;
    @FXML private TableColumn<Habit, Void> actionCol;
    @FXML private FlowPane gardenPane;

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

        // pressing Enter in the name field also adds the habit
        nameField.setOnAction(e -> handleAddHabit());

        // table columns
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHabitName()));
        frequencyCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFrequency()));
        plantCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlantType()));
        stageCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlant().getStageName()));
        streakCol.setCellValueFactory(c -> new SimpleStringProperty(streakText(c.getValue())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(statusOf(c.getValue())));
        setupActionColumn();

        habitTable.setItems(habitList);
        habitTable.setPlaceholder(new Label("No habits yet. Add your first one above!"));

        loadSavedHabits();
        if (DEMO_MODE && manager.getHabits().isEmpty()) {
            addDemoData();
        }
        refreshUI();

        // re-check every minute so Done buttons re-enable after midnight
        Timeline dayCheck = new Timeline(new KeyFrame(Duration.minutes(1), e -> refreshUI()));
        dayCheck.setCycleCount(Timeline.INDEFINITE);
        dayCheck.play();
    }

    /** Member 3 hook: load habits from the CSV file when the app starts. */
    private void loadSavedHabits() {
        // TODO (Member 3 / integration):
        // try {
        //     for (Habit h : storage.loadHabits()) {
        //         try {
        //             manager.addHabit(h);          // one bad row is skipped,
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
    }

    // ======================================================================
    // Button handlers
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

            manager.addHabit(name, frequency, plant);   // Member 1's code validates the input

            nameField.clear();
            nameField.requestFocus();
            persist();
            refreshUI();
            showMessage("Planted a new " + plant.toLowerCase() + " for \"" + name.trim() + "\".");
        } catch (IllegalArgumentException e) {
            showError("Cannot add habit", e.getMessage());
        }
    }

    private void completeHabit(Habit habit) {
        try {
            manager.completeHabit(habit);
            persist();
            refreshUI();
            showMessage("Nice! \"" + habit.getHabitName() + "\" streak is now " + habit.getStreak() + ".");
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError("Cannot complete habit", e.getMessage());
        }
    }

    private void deleteHabit(Habit habit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + habit.getHabitName() + "\" and its plant?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete habit");
        confirm.setHeaderText("Are you sure?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            manager.deleteHabit(habit);
            persist();
            refreshUI();
            showMessage("Deleted \"" + habit.getHabitName() + "\".");
        }
    }

    // ======================================================================
    // Refreshing the screen
    // ======================================================================

    /** Rebuilds the table, the garden and the summary from the current model. */
    private void refreshUI() {
        manager.refreshAll();                       // updates wilted / growth stage by today's date
        habitList.setAll(manager.getHabits());
        habitTable.refresh();
        buildGarden();
        updateSummary();
    }

    private void updateSummary() {
        long due = manager.getHabits().stream().filter(Habit::isDue).count();
        summaryLabel.setText(manager.getHabits().size() + " habits  |  " + due + " due");
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }

    // ---------- table: Done / Delete buttons in every row ----------

    private void setupActionColumn() {
        actionCol.setCellFactory(col -> new TableCell<Habit, Void>() {
            private final Button doneBtn = new Button("Done");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, doneBtn, deleteBtn);

            {
                doneBtn.getStyleClass().add("done-button");
                deleteBtn.getStyleClass().add("delete-button");
                doneBtn.setOnAction(e -> completeHabit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteHabit(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                Habit habit = getTableView().getItems().get(getIndex());
                doneBtn.setDisable(!habit.isDue());
                setGraphic(box);
            }
        });
    }

    // ---------- garden: one card per plant ----------

    private void buildGarden() {
        gardenPane.getChildren().clear();

        if (manager.getHabits().isEmpty()) {
            Label empty = new Label("Your garden is empty. Add a habit to plant your first seed.");
            empty.getStyleClass().add("empty-label");
            gardenPane.getChildren().add(empty);
            return;
        }

        for (Habit habit : manager.getHabits()) {
            gardenPane.getChildren().add(createPlantCard(habit));
        }
    }

    private VBox createPlantCard(Habit habit) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(180);
        card.getStyleClass().add("plant-card");
        if (habit.getPlant().isWilted()) {
            card.getStyleClass().add("plant-card-wilted");
        }

        Label name = new Label(habit.getHabitName());
        name.getStyleClass().add("card-name");
        name.setWrapText(true);

        Label streak = new Label("Streak: " + streakText(habit));
        streak.getStyleClass().add("card-info");

        Label stage = new Label(habit.getPlant().getStageName() + "  |  " + habit.getFrequency());
        stage.getStyleClass().add("card-info");

        Label status = new Label(statusOf(habit));
        status.getStyleClass().add(statusStyle(habit));

        Button done = new Button("Done");
        done.getStyleClass().add("done-button");
        done.setDisable(!habit.isDue());
        done.setOnAction(e -> completeHabit(habit));

        card.getChildren().addAll(createPlantPicture(habit), name, streak, stage, status, done);
        return card;
    }

    /** Shows the plant image for the current stage; falls back to text if the file is missing. */
    private Node createPlantPicture(Habit habit) {
        Image image = loadImage(habit.getPlant().getPlantImage());
        if (image != null) {
            ImageView view = new ImageView(image);
            view.setFitWidth(110);
            view.setFitHeight(110);
            view.setPreserveRatio(true);
            return view;
        }
        Label placeholder = new Label(habit.getPlant().getStageName());
        placeholder.getStyleClass().add("image-placeholder");
        placeholder.setPrefSize(110, 110);
        placeholder.setAlignment(Pos.CENTER);
        return placeholder;
    }

    private Image loadImage(String fileName) {
        if (imageCache.containsKey(fileName)) {
            return imageCache.get(fileName);
        }
        Image image = null;
        try (InputStream in = getClass().getResourceAsStream("/images/" + fileName)) {
            if (in != null) {
                image = new Image(in);
            }
        } catch (Exception e) {
            // image could not be read: the text placeholder is used instead
        }
        imageCache.put(fileName, image);
        return image;
    }

    // ---------- status text and style ----------

    /** A missed habit's streak is already broken, so show 0 and the old value. */
    private String streakText(Habit habit) {
        if (habit.isMissed()) {
            return "0 (was " + habit.getStreak() + ")";
        }
        return String.valueOf(habit.getStreak());
    }

    private String statusOf(Habit habit) {
        if (habit.getPlant().isWilted()) {
            return "Wilted - needs care";
        }
        if (habit.isDue()) {
            return "Due";
        }
        return "Done for now";
    }

    private String statusStyle(Habit habit) {
        if (habit.getPlant().isWilted()) {
            return "status-wilted";
        }
        return habit.isDue() ? "status-due" : "status-done";
    }

    // ---------- alerts ----------

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
