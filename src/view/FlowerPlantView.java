package view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.transform.Rotate;
import model.Habit;
import model.Plant;

/** A flower: seed, sprout, pink bud, then a full bloom with eight petals. */
public class FlowerPlantView extends PlantView {

    public FlowerPlantView(Habit habit) {
        super(habit);
    }

    @Override
    protected String[] stageNames() {
        return new String[]{"Seed", "Sprout", "Bud", "In bloom"};
    }

    @Override
    public String getTypeLabel() {
        return "Flower";
    }

    @Override
    protected void drawStage(Group g, int stage) {
        switch (stage) {
            case Plant.SEED:
                g.getChildren().add(ball(0, -3, 5, "#795548", "#4e342e"));
                break;

            case Plant.SPROUT:
                g.getChildren().addAll(
                        stem(28, 4, "#7cb342", 3),
                        leaf(-8, -14, -35, 16, "#8bc34a"),
                        leaf(9, -20, 35, 15, "#9ccc65"));
                break;

            case Plant.BUD:
                g.getChildren().addAll(
                        stem(62, 6, "#689f38", 3.5),
                        leaf(-10, -22, -30, 22, "#7cb342"),
                        leaf(11, -34, 30, 20, "#8bc34a"));
                Ellipse bud = new Ellipse(2.4, -70, 7, 11);
                bud.setFill(Color.web("#ec407a"));
                bud.setStroke(Color.web("#c2185b"));
                g.getChildren().addAll(bud,
                        leaf(-2, -61, -40, 10, "#7cb342"),
                        leaf(7, -61, 40, 10, "#7cb342"));
                break;

            default: // Plant.FLOWER - full bloom
                double cx = 2.4;
                double cy = -90;
                g.getChildren().addAll(
                        stem(90, 6, "#689f38", 4),
                        leaf(-11, -26, -30, 24, "#7cb342"),
                        leaf(12, -42, 30, 22, "#8bc34a"),
                        leaf(-10, -62, -25, 18, "#9ccc65"));
                for (int i = 0; i < 8; i++) {
                    Ellipse petal = new Ellipse(cx, cy - 13, 7, 13);
                    petal.setFill(Color.web(i % 2 == 0 ? "#f06292" : "#f48fb1"));
                    petal.setStroke(Color.web("#d81b60"));
                    petal.getTransforms().add(new Rotate(i * 45, cx, cy));
                    g.getChildren().add(petal);
                }
                g.getChildren().add(ball(cx, cy, 8, "#ffd54f", "#f9a825"));
                break;
        }
    }
}
