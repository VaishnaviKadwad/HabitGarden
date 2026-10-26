package view;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import model.Habit;
import model.Plant;

import java.util.ArrayList;
import java.util.List;

/**
 * One plant standing in the garden, drawn with JavaFX shapes.
 *
 * OOP: this is an abstract class. FlowerPlantView, TreePlantView and
 * CactusPlantView each override drawStage() to draw their own shape for every
 * growth stage (polymorphism). The point (0, 0) is where the plant meets the
 * soil, and the plant is drawn upwards (negative y).
 */
public abstract class PlantView extends Group {

    protected final Habit habit;

    private final Group body = new Group();                 // the plant itself
    private final Rotate sway = new Rotate(0, 0, 0);        // swaying in the wind / drooping
    private final Scale pop = new Scale(1, 1, 0, 0);        // "grow" animation
    private final Scale depth = new Scale(1, 1, 0, 0);      // smaller when further back
    private final Ellipse ring = new Ellipse(0, 3, 40, 11); // selection highlight
    private final List<Animation> animations = new ArrayList<>();
    private FadeTransition ringPulse;
    private double plantTop = -20;

    protected PlantView(Habit habit) {
        this.habit = habit;
    }

    /** Factory: picks the right drawing class for the habit's plant type. */
    public static PlantView create(Habit habit) {
        switch (habit.getPlantType()) {
            case "TREE":
                return new TreePlantView(habit);
            case "CACTUS":
                return new CactusPlantView(habit);
            default:
                return new FlowerPlantView(habit);
        }
    }

    // ------------------------------------------------------------------
    // What every plant type must provide (overridden in the subclasses)
    // ------------------------------------------------------------------

    /** Draw the plant for a growth stage (Plant.SEED ... Plant.FLOWER) into the group. */
    protected abstract void drawStage(Group canvas, int stage);

    /** Display names of the four stages, e.g. "Sapling" for a tree. */
    protected abstract String[] stageNames();

    /** "Flower", "Tree" or "Cactus". */
    public abstract String getTypeLabel();

    /** How strongly this plant sways in the wind (1 = a lot, 0 = not at all). */
    protected double swayStrength() {
        return 1.0;
    }

    // ------------------------------------------------------------------
    // Building the plant
    // ------------------------------------------------------------------

    /** Draws the plant, its name sign and its speech bubble, and starts the animations. */
    public void build() {
        int stage = habit.getPlant().getGrowthStage();
        boolean wilted = habit.getPlant().isWilted();

        Ellipse soil = new Ellipse(0, 2, 30, 8);
        soil.setFill(Color.web("#6d4c41"));
        Ellipse soilTop = new Ellipse(0, 0, 24, 5);
        soilTop.setFill(Color.web("#8d6e63"));

        drawStage(body, stage);
        plantTop = Math.min(-20, body.getBoundsInLocal().getMinY());
        body.getTransforms().addAll(pop, sway);
        if (wilted) {
            body.setEffect(new SepiaTone(0.75));   // brown, dried-out colours
        }

        ring.setFill(null);
        ring.setStroke(Color.web("#fff176"));
        ring.setStrokeWidth(3);
        ring.setVisible(false);
        ringPulse = new FadeTransition(Duration.millis(700), ring);
        ringPulse.setFromValue(1.0);
        ringPulse.setToValue(0.3);
        ringPulse.setAutoReverse(true);
        ringPulse.setCycleCount(Animation.INDEFINITE);

        VBox sign = makeSign();
        sign.setLayoutY(16);
        sign.layoutXProperty().bind(sign.widthProperty().divide(-2));

        getChildren().addAll(ring, soil, soilTop, body, sign);

        Label bubble = makeBubble(wilted);
        if (bubble != null) {
            bubble.setLayoutY(plantTop - 32);
            bubble.layoutXProperty().bind(bubble.widthProperty().divide(-2));
            getChildren().add(bubble);
            TranslateTransition bob = new TranslateTransition(Duration.seconds(1.1), bubble);
            bob.setByY(-5);
            bob.setAutoReverse(true);
            bob.setCycleCount(Animation.INDEFINITE);
            start(bob);
        }

        getTransforms().add(depth);
        setPickOnBounds(true);              // clicking anywhere on the plant counts
        setCursor(Cursor.HAND);
        Tooltip.install(this, new Tooltip(tooltipText()));
        setOnMouseEntered(e -> setEffect(new DropShadow(22, Color.web("#fff59d"))));
        setOnMouseExited(e -> setEffect(null));

        startSway(stage, wilted);
    }

    private void startSway(int stage, boolean wilted) {
        if (stage == Plant.SEED) {
            return;                                   // seeds don't move
        }
        double base = wilted ? 12 : 0;                // a wilted plant leans over
        double amount = (wilted ? 1.0 : (stage == Plant.FLOWER ? 3.0 : 2.0)) * swayStrength();
        double seconds = wilted ? 3.5 : 1.8 + Math.random() * 1.4;
        sway.setAngle(base - amount);
        Timeline swayAnimation = new Timeline(new KeyFrame(Duration.seconds(seconds),
                new KeyValue(sway.angleProperty(), base + amount, Interpolator.EASE_BOTH)));
        swayAnimation.setAutoReverse(true);
        swayAnimation.setCycleCount(Animation.INDEFINITE);
        start(swayAnimation);
    }

    private VBox makeSign() {
        Label name = new Label(habit.getHabitName());
        name.getStyleClass().add("sign-name");
        name.setMaxWidth(130);
        String streak = habit.isMissed() ? "streak broken" : "streak " + habit.getStreak();
        Label info = new Label(streak);
        info.getStyleClass().add("sign-info");
        VBox sign = new VBox(1, name, info);
        sign.setAlignment(Pos.CENTER);
        sign.getStyleClass().add("plant-sign");
        return sign;
    }

    private Label makeBubble(boolean wilted) {
        if (wilted) {
            Label bubble = new Label("needs care!");
            bubble.getStyleClass().add("bubble-wilted");
            return bubble;
        }
        if (habit.isDue()) {
            Label bubble = new Label("water me");
            bubble.getStyleClass().add("bubble-due");
            return bubble;
        }
        return null;                                  // already watered: no bubble
    }

    private String tooltipText() {
        String streak = habit.isMissed() ? "streak broken" : "streak " + habit.getStreak();
        return habit.getHabitName() + "\n"
                + getTypeLabel() + " - " + getStageName() + "\n"
                + habit.getFrequency().toLowerCase() + ", " + streak + "\n\n"
                + "Click to select, double-click to water.";
    }

    // ------------------------------------------------------------------
    // Animations used by GardenView
    // ------------------------------------------------------------------

    /** Quick "boing": the plant scales from 'from' up past full size and settles. */
    public void playPop(double from) {
        Timeline grow = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(pop.xProperty(), from),
                        new KeyValue(pop.yProperty(), from)),
                new KeyFrame(Duration.millis(380),
                        new KeyValue(pop.xProperty(), 1.12, Interpolator.EASE_OUT),
                        new KeyValue(pop.yProperty(), 1.12, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(pop.xProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(pop.yProperty(), 1.0, Interpolator.EASE_BOTH)));
        start(grow);
    }

    public void setSelected(boolean selected) {
        ring.setVisible(selected);
        if (ringPulse == null) {
            return;
        }
        if (selected) {
            ringPulse.play();
        } else {
            ringPulse.stop();
        }
    }

    /** Stops all animations (called before the garden is redrawn). */
    public void dispose() {
        for (Animation animation : animations) {
            animation.stop();
        }
        animations.clear();
        if (ringPulse != null) {
            ringPulse.stop();
        }
    }

    private void start(Animation animation) {
        animations.add(animation);
        animation.play();
    }

    // ------------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------------

    public Habit getHabit() {
        return habit;
    }

    public String getStageName() {
        return getStageName(habit.getPlant().getGrowthStage());
    }

    public String getStageName(int stage) {
        return stageNames()[stage];
    }

    /** y of the top of the plant (a negative number, before the depth scale). */
    public double getPlantTop() {
        return plantTop;
    }

    public double getDepth() {
        return depth.getX();
    }

    public void setDepth(double scale) {
        depth.setX(scale);
        depth.setY(scale);
    }

    // ------------------------------------------------------------------
    // Small drawing helpers shared by the subclasses
    // ------------------------------------------------------------------

    /** A curved stem from the soil (0,0) up to (bend * 0.4, -height). */
    protected QuadCurve stem(double height, double bend, String color, double width) {
        QuadCurve stem = new QuadCurve(0, 0, bend, -height * 0.5, bend * 0.4, -height);
        stem.setFill(null);
        stem.setStroke(Color.web(color));
        stem.setStrokeWidth(width);
        stem.setStrokeLineCap(StrokeLineCap.ROUND);
        return stem;
    }

    /** An oval leaf centred at (x, y), tilted by 'angle' degrees. */
    protected Ellipse leaf(double x, double y, double angle, double length, String color) {
        Ellipse leaf = new Ellipse(x, y, length / 2, length / 5);
        leaf.setRotate(angle);
        leaf.setFill(Color.web(color));
        leaf.setStroke(Color.web("#33691e"));
        leaf.setStrokeWidth(0.8);
        return leaf;
    }

    /** A filled circle with an outline. */
    protected Circle ball(double x, double y, double radius, String fill, String stroke) {
        Circle circle = new Circle(x, y, radius);
        circle.setFill(Color.web(fill));
        circle.setStroke(Color.web(stroke));
        circle.setStrokeWidth(1);
        return circle;
    }
}
