package model;

/** A flower grows quickly: it blooms after a streak of 6. */
public class Flower extends Plant {

    @Override
    public void grow(int streak) {
        if (streak >= 6) {
            setGrowthStage(FLOWER);
        } else if (streak >= 3) {
            setGrowthStage(BUD);
        } else if (streak >= 1) {
            setGrowthStage(SPROUT);
        } else {
            setGrowthStage(SEED);
        }
    }

    @Override
    public String getPlantImage() {
        return buildImageName();
    }

    @Override
    public String getTypeName() {
        return "flower";
    }
}
