package model;

/** A cactus is hardy: it is fully grown after a streak of 8. */
public class Cactus extends Plant {

    @Override
    public void grow(int streak) {
        if (streak >= 8) {
            setGrowthStage(FLOWER);
        } else if (streak >= 4) {
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
        return "cactus";
    }
}
