package model;

/** A tree grows slowly: it is fully grown after a streak of 10. */
public class Tree extends Plant {

    @Override
    public void grow(int streak) {
        if (streak >= 10) {
            setGrowthStage(FLOWER);
        } else if (streak >= 5) {
            setGrowthStage(BUD);
        } else if (streak >= 2) {
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
        return "tree";
    }
}
