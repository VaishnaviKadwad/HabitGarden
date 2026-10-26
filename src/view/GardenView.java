package view;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import model.Habit;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * The garden scene: sky (changes with the time of day), drifting clouds,
 * hills, a fence, grass, and one PlantView per habit arranged in rows.
 *
 * It only draws. When the user clicks a plant it tells the controller
 * through the onSelect / onWater callbacks (the controller decides what happens).
 */
public class GardenView extends Pane {

    private final Rectangle sky = new Rectangle();
    private final Circle sunOrMoon = new Circle(32);
    private final Group stars = new Group();
    private final Group clouds = new Group();
    private final Ellipse backHill = new Ellipse();
    private final Ellipse frontHill = new Ellipse();
    private final Group fence = new Group();
    private final Rectangle ground = new Rectangle();
    private final Group plantLayer = new Group();
    private final Group effectLayer = new Group();
    private final Label emptyLabel = new Label("Your garden is empty.\nPlant your first habit on the right!");

    private final List<PlantView> plantViews = new ArrayList<>();
    private final Consumer<Habit> onSelect;
    private final Consumer<Habit> onWater;
    private final Random random = new Random();
    private double groundTop;

    public GardenView(Consumer<Habit> onSelect, Consumer<Habit> onWater, Runnable onEmptyClick) {
        this.onSelect = onSelect;
        this.onWater = onWater;

        setPrefSize(640, 480);
        setMinSize(300, 250);

        // keep drawings (e.g. clouds) inside the garden area
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        emptyLabel.getStyleClass().add("garden-empty");
        emptyLabel.setTextAlignment(TextAlignment.CENTER);
        emptyLabel.layoutXProperty().bind(widthProperty().subtract(emptyLabel.widthProperty()).divide(2));
        emptyLabel.layoutYProperty().bind(heightProperty().multiply(0.62));
        emptyLabel.setMouseTransparent(true);
        effectLayer.setMouseTransparent(true);

        backHill.setFill(Color.web("#9ccc65"));
        frontHill.setFill(Color.web("#8bc34a"));
        ground.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#8bc34a")), new Stop(1, Color.web("#5a8f29"))));

        createClouds();
        getChildren().addAll(sky, stars, sunOrMoon, clouds, backHill, frontHill,
                fence, ground, plantLayer, effectLayer, emptyLabel);

        setOnMouseClicked(e -> onEmptyClick.run());           // click on empty ground = deselect
        widthProperty().addListener((obs, oldValue, newValue) -> relayout());
        heightProperty().addListener((obs, oldValue, newValue) -> relayout());

        updateSky();
        startCloudAnimation();
    }

    // ======================================================================
    // Public methods used by the controller
    // ======================================================================

    /** Redraws every plant from the current list of habits. */
    public void setHabits(List<Habit> habits, Habit selected) {
        for (PlantView view : plantViews) {
            view.dispose();
        }
        plantViews.clear();
        plantLayer.getChildren().clear();

        for (Habit habit : habits) {
            PlantView view = PlantView.create(habit);
            view.build();
            view.setSelected(habit == selected);
            view.setOnMouseClicked(e -> {
                e.consume();                                    // don't also deselect
                if (e.getClickCount() >= 2) {
                    onWater.accept(habit);
                } else {
                    onSelect.accept(habit);
                }
            });
            plantViews.add(view);
            plantLayer.getChildren().add(view);
        }
        relayout();
    }

    /** Moves the yellow highlight ring to the selected plant. */
    public void setSelected(Habit selected) {
        for (PlantView view : plantViews) {
            view.setSelected(view.getHabit() == selected);
        }
    }

    /** A new plant grows up out of the soil. */
    public void sproutIn(Habit habit) {
        PlantView view = viewFor(habit);
        if (view != null) {
            view.playPop(0.0);
        }
    }

    /** Water drops fall on the plant; if it reached a new stage, sparkles and a label too. */
    public void celebrate(Habit habit, boolean grewToNewStage) {
        PlantView view = viewFor(habit);
        if (view == null) {
            return;
        }
        view.playPop(0.85);

        double x = view.getLayoutX();
        double top = view.getLayoutY() + view.getPlantTop() * view.getDepth();

        for (int i = 0; i < 9; i++) {
            Ellipse drop = new Ellipse(x + random.nextDouble() * 60 - 30,
                    top - 60 - random.nextDouble() * 30, 2.5, 4.5);
            drop.setFill(Color.web("#4fc3f7"));
            drop.setOpacity(0);
            playAndRemove(drop, 0, 60 + random.nextDouble() * 25, 650, i * 70);
        }

        if (grewToNewStage) {
            for (int i = 0; i < 12; i++) {
                double angle = Math.toRadians(i * 30);
                Circle spark = new Circle(x, top + 10, 3.5,
                        Color.web(i % 2 == 0 ? "#fff176" : "#ffffff"));
                spark.setOpacity(0);
                playAndRemove(spark, Math.cos(angle) * 60, Math.sin(angle) * 60, 900, 250);
            }
            Label text = new Label(view.getStageName() + "!");
            text.getStyleClass().add("float-text");
            text.layoutXProperty().bind(text.widthProperty().divide(-2).add(x));
            text.setLayoutY(top - 40);
            text.setOpacity(0);
            playAndRemove(text, 0, -40, 1400, 300);
        }
    }

    /** Sets the sky colours for the current time: morning, day, evening or night. */
    public void updateSky() {
        int hour = LocalTime.now().getHour();
        Color top;
        Color bottom;
        Color orb;
        boolean night = false;
        if (hour >= 5 && hour < 8) {            // morning
            top = Color.web("#6fb7e6");
            bottom = Color.web("#ffd9a8");
            orb = Color.web("#ffb74d");
        } else if (hour >= 8 && hour < 17) {    // day
            top = Color.web("#3d8fd6");
            bottom = Color.web("#bfe6ff");
            orb = Color.web("#ffeb3b");
        } else if (hour >= 17 && hour < 19) {   // evening
            top = Color.web("#4a3b7a");
            bottom = Color.web("#ff9e6d");
            orb = Color.web("#ff7043");
        } else {                                // night
            top = Color.web("#0b1636");
            bottom = Color.web("#2b3f72");
            orb = Color.web("#f5f3ce");
            night = true;
        }
        sky.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, top), new Stop(1, bottom)));
        sunOrMoon.setFill(orb);
        sunOrMoon.setEffect(new DropShadow(BlurType.GAUSSIAN, orb, 40, 0.3, 0, 0));
        stars.setVisible(night);
        clouds.setOpacity(night ? 0.35 : 1.0);
        relayout();
    }

    // ======================================================================
    // Layout: called whenever the window is resized or plants change
    // ======================================================================

    private void relayout() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        groundTop = h * 0.42;

        sky.setWidth(w);
        sky.setHeight(groundTop + 80);
        sunOrMoon.setCenterX(w * 0.82);
        sunOrMoon.setCenterY(groundTop * 0.32);

        backHill.setCenterX(w * 0.22);
        backHill.setCenterY(groundTop + 30);
        backHill.setRadiusX(w * 0.42);
        backHill.setRadiusY(70);
        frontHill.setCenterX(w * 0.78);
        frontHill.setCenterY(groundTop + 40);
        frontHill.setRadiusX(w * 0.40);
        frontHill.setRadiusY(60);

        ground.setX(0);
        ground.setY(groundTop + 20);
        ground.setWidth(w);
        ground.setHeight(h - groundTop - 20);

        rebuildFence(w);
        placeStars(w);
        for (Node cloud : clouds.getChildren()) {
            cloud.setLayoutY(groundTop * (Double) cloud.getUserData());
        }
        layoutPlants(w, h);
    }

    /**
     * Puts the plants in rows, like a real garden bed: the tallest plants go in
     * the back row, back rows sit higher and smaller, and rows are staggered so
     * plants don't hide each other.
     */
    private void layoutPlants(double w, double h) {
        int count = plantViews.size();
        emptyLabel.setVisible(count == 0);
        if (count == 0) {
            return;
        }

        // tallest first (plantTop is negative, so the smallest number is the tallest plant)
        List<PlantView> byHeight = new ArrayList<>(plantViews);
        byHeight.sort((a, b) -> Double.compare(a.getPlantTop(), b.getPlantTop()));
        plantLayer.getChildren().setAll(byHeight);     // back row drawn first, front row on top

        double margin = 50;
        double usable = Math.max(100, w - 2 * margin);
        int maxPerRow = Math.max(1, (int) (usable / 160));
        int rows = (int) Math.ceil(count / (double) maxPerRow);
        int perRow = (int) Math.ceil(count / (double) rows);      // spread plants evenly over the rows

        double frontDepth = 1.2;
        double frontY = h - 62 * frontDepth;                      // leave room for the name signs
        double backY = Math.min(frontY, groundTop + 85);
        if (rows == 1) {
            backY = frontY = Math.min(frontY, groundTop + (h - groundTop) * 0.55);
        }

        for (int i = 0; i < count; i++) {
            int row = i / perRow;
            int col = i % perRow;
            int inThisRow = Math.min(perRow, count - row * perRow);
            double t = rows == 1 ? 1.0 : row / (double) (rows - 1);   // 0 = back row, 1 = front row
            double spacing = usable / inThisRow;
            double stagger = rows == 1 ? 0 : (row % 2 == 0 ? -0.18 : 0.18) * spacing;

            PlantView view = byHeight.get(i);
            view.setLayoutX(margin + spacing * (col + 0.5) + stagger);
            view.setLayoutY(backY + (frontY - backY) * t);
            view.setDepth(0.95 + (frontDepth - 0.95) * t);
        }
    }

    private void rebuildFence(double w) {
        fence.getChildren().clear();
        double base = groundTop + 22;
        Color wood = Color.web("#d7b98e");
        Color edge = Color.web("#a1887f");
        for (double railY : new double[]{base - 24, base - 12}) {
            Rectangle rail = new Rectangle(0, railY, w, 5);
            rail.setFill(wood);
            rail.setStroke(edge);
            fence.getChildren().add(rail);
        }
        for (double x = 8; x < w; x += 44) {
            Rectangle post = new Rectangle(x, base - 32, 10, 32);
            post.setArcWidth(6);
            post.setArcHeight(6);
            post.setFill(wood);
            post.setStroke(edge);
            fence.getChildren().add(post);
        }
    }

    private void placeStars(double w) {
        stars.getChildren().clear();
        Random fixed = new Random(7);   // same pattern every time
        for (int i = 0; i < 45; i++) {
            Circle star = new Circle(fixed.nextDouble() * w, fixed.nextDouble() * groundTop * 0.9,
                    0.6 + fixed.nextDouble() * 1.4,
                    Color.rgb(255, 255, 255, 0.5 + fixed.nextDouble() * 0.5));
            stars.getChildren().add(star);
        }
    }

    // ======================================================================
    // Clouds
    // ======================================================================

    private void createClouds() {
        for (int i = 0; i < 4; i++) {
            Ellipse[] puffs = {
                    new Ellipse(0, 0, 38, 16),
                    new Ellipse(-26, 6, 24, 12),
                    new Ellipse(26, 6, 26, 12),
                    new Ellipse(8, -10, 22, 14)
            };
            for (Ellipse puff : puffs) {
                puff.setFill(Color.rgb(255, 255, 255, 0.85));
            }
            Group cloud = new Group(puffs);
            double size = 0.7 + random.nextDouble() * 0.6;
            cloud.setScaleX(size);
            cloud.setScaleY(size);
            cloud.setLayoutX(i * 260 + random.nextInt(80));
            cloud.setUserData(0.2 + 0.45 * random.nextDouble());   // height as a fraction of the sky
            clouds.getChildren().add(cloud);
        }
    }

    /** Moves the clouds slowly to the right, every frame. */
    private void startCloudAnimation() {
        AnimationTimer timer = new AnimationTimer() {
            private long last = 0;

            @Override
            public void handle(long now) {
                if (last > 0) {
                    double seconds = (now - last) / 1_000_000_000.0;
                    for (Node cloud : clouds.getChildren()) {
                        double x = cloud.getLayoutX() + seconds * 12 * cloud.getScaleX();
                        if (x > getWidth() + 120) {
                            x = -120;
                        }
                        cloud.setLayoutX(x);
                    }
                }
                last = now;
            }
        };
        timer.start();
    }

    // ======================================================================
    // Helpers
    // ======================================================================

    private PlantView viewFor(Habit habit) {
        for (PlantView view : plantViews) {
            if (view.getHabit() == habit) {
                return view;
            }
        }
        return null;
    }

    /** Adds a node to the effect layer, moves + fades it, then removes it. */
    private void playAndRemove(Node node, double byX, double byY, double millis, double delayMillis) {
        effectLayer.getChildren().add(node);
        TranslateTransition move = new TranslateTransition(Duration.millis(millis), node);
        move.setByX(byX);
        move.setByY(byY);
        FadeTransition fade = new FadeTransition(Duration.millis(millis), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        ParallelTransition both = new ParallelTransition(move, fade);
        both.setDelay(Duration.millis(delayMillis));
        both.setOnFinished(e -> effectLayer.getChildren().remove(node));
        both.play();
    }
}
