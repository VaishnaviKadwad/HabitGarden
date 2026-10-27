package view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import model.Habit;
import model.Plant;

/** A cactus: seeds, a round baby cactus, a young cactus with one arm, then a flowering cactus. */
public class CactusPlantView extends PlantView {

    public CactusPlantView(Habit habit) {
        super(habit);
    }

    @Override
    protected String[] stageNames() {
        return new String[]{"Seed", "Baby cactus", "Young cactus", "Flowering cactus"};
    }

    @Override
    public String getTypeLabel() {
        return "Cactus";
    }

    @Override
    protected double swayStrength() {
        return 0.25;  // a cactus barely moves
    }

    @Override
    protected void drawStage(Group g, int stage) {
        switch (stage) {
            case Plant.SEED:
                g.getChildren().addAll(
                        ball(-3, -3, 3.5, "#4e342e", "#3e2723"),
                        ball(4, -2, 2.5, "#5d4037", "#3e2723"));
                break;

            case Plant.SPROUT: // baby cactus
                g.getChildren().add(ball(0, -12, 12, "#7cb342", "#33691e"));
                addSpines(g, new double[][]{{-8, -16}, {6, -20}, {0, -8}, {9, -9}});
                break;

            case Plant.BUD: // young cactus with one arm
                g.getChildren().addAll(
                        pad(10, -34, 14, 9),
                        pad(18, -50, 9, 24),
                        pad(-12, -58, 24, 58),
                        ridge(0, -52, 0, -4));
                addSpines(g, new double[][]{{-8, -44}, {6, -30}, {-6, -18}, {7, -50}, {21, -44}});
                break;

            default: // Plant.FLOWER - tall cactus with two arms and a pink flower
                g.getChildren().addAll(
                        pad(-31, -50, 20, 11),
                        pad(-33, -76, 11, 34),
                        pad(12, -40, 20, 11),
                        pad(22, -64, 11, 32),
                        pad(-15, -90, 30, 90),
                        ridge(-6, -82, -6, -6),
                        ridge(6, -82, 6, -6));
                addSpines(g, new double[][]{{-11, -70}, {10, -56}, {-10, -34}, {11, -20}, {0, -60}, {-28, -68}, {26, -56}});
                for (int i = 0; i < 5; i++) {
                    Ellipse petal = new Ellipse(0, -100, 5, 9);
                    petal.setFill(Color.web("#f48fb1"));
                    petal.setStroke(Color.web("#ec407a"));
                    petal.getTransforms().add(new Rotate(i * 72, 0, -92));
                    g.getChildren().add(petal);
                }
                g.getChildren().add(ball(0, -92, 4, "#ffeb3b", "#fbc02d"));
                break;
        }
    }

    /** A rounded green block: the body or an arm of the cactus. */
    private Rectangle pad(double x, double y, double width, double height) {
        Rectangle pad = new Rectangle(x, y, width, height);
        double round = Math.min(width, height);
        pad.setArcWidth(round);
        pad.setArcHeight(round);
        pad.setFill(Color.web("#7cb342"));
        pad.setStroke(Color.web("#33691e"));
        pad.setStrokeWidth(1.2);
        return pad;
    }

    private Line ridge(double x1, double y1, double x2, double y2) {
        Line ridge = new Line(x1, y1, x2, y2);
        ridge.setStroke(Color.web("#558b2f"));
        ridge.setStrokeWidth(1.5);
        return ridge;
    }

    /** Tiny white "X" marks for spines. */
    private void addSpines(Group g, double[][] points) {
        for (double[] p : points) {
            Line a = new Line(p[0] - 2, p[1] - 2, p[0] + 2, p[1] + 2);
            Line b = new Line(p[0] - 2, p[1] + 2, p[0] + 2, p[1] - 2);
            a.setStroke(Color.web("#f1f8e9"));
            b.setStroke(Color.web("#f1f8e9"));
            g.getChildren().addAll(a, b);
        }
    }
}
