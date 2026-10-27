package view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import model.Habit;
import model.Plant;

/** A tree: acorn, seedling, sapling, then a big leafy tree with red fruit. */
public class TreePlantView extends PlantView {

    public TreePlantView(Habit habit) {
        super(habit);
    }

    @Override
    protected String[] stageNames() {
        return new String[]{"Seed", "Seedling", "Sapling", "Full tree"};
    }

    @Override
    public String getTypeLabel() {
        return "Tree";
    }

    @Override
    protected double swayStrength() {
        return 0.4;   // trees are stiff
    }

    @Override
    protected void drawStage(Group g, int stage) {
        switch (stage) {
            case Plant.SEED:
                Ellipse acorn = new Ellipse(0, -5, 6, 6);
                acorn.setFill(Color.web("#a1887f"));
                Ellipse cap = new Ellipse(0, -10, 7, 3.5);
                cap.setFill(Color.web("#5d4037"));
                g.getChildren().addAll(acorn, cap);
                break;

            case Plant.SPROUT:
                g.getChildren().addAll(
                        stem(26, 3, "#795548", 3),
                        leaf(-7, -22, -25, 14, "#66bb6a"),
                        leaf(8, -25, 25, 13, "#81c784"));
                break;

            case Plant.BUD: // sapling
                g.getChildren().addAll(
                        trunk(4, 2.5, 52, "#8d6e63"),
                        ball(-14, -54, 13, "#4caf50", "#2e7d32"),
                        ball(14, -56, 13, "#43a047", "#2e7d32"),
                        ball(0, -64, 20, "#66bb6a", "#388e3c"));
                break;

            default: // Plant.FLOWER - full tree
                g.getChildren().addAll(
                        trunk(10, 6, 95, "#795548"),
                        branch(0, -70, -22, -92),
                        branch(0, -78, 20, -98),
                        ball(-30, -104, 30, "#388e3c", "#1b5e20"),
                        ball(30, -106, 30, "#43a047", "#1b5e20"),
                        ball(0, -124, 40, "#4caf50", "#2e7d32"),
                        ball(-16, -146, 24, "#66bb6a", "#388e3c"),
                        ball(18, -144, 22, "#81c784", "#43a047"),
                        ball(-20, -112, 4, "#e53935", "#b71c1c"),
                        ball(14, -128, 4, "#e53935", "#b71c1c"),
                        ball(32, -100, 4, "#e53935", "#b71c1c"),
                        ball(-4, -146, 4, "#e53935", "#b71c1c"));
                break;
        }
    }

    /** A trunk that is wider at the bottom than at the top. */
    private Polygon trunk(double bottomHalfWidth, double topHalfWidth, double height, String color) {
        Polygon trunk = new Polygon(
                -bottomHalfWidth, 0,
                bottomHalfWidth, 0,
                topHalfWidth, -height,
                -topHalfWidth, -height);
        trunk.setFill(Color.web(color));
        trunk.setStroke(Color.web("#4e342e"));
        return trunk;
    }

    private Line branch(double x1, double y1, double x2, double y2) {
        Line branch = new Line(x1, y1, x2, y2);
        branch.setStroke(Color.web("#6d4c41"));
        branch.setStrokeWidth(4);
        branch.setStrokeLineCap(StrokeLineCap.ROUND);
        return branch;
    }
}
