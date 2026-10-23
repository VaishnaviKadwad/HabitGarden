package model;

/**
 * Abstract base class for every plant in the garden.
 * (Abstraction + Inheritance: Flower, Tree and Cactus extend this class.)
 */
public abstract class Plant {

    // Growth stages: 0 = seed, 1 = sprout, 2 = bud, 3 = flower
    public static final int SEED = 0;
    public static final int SPROUT = 1;
    public static final int BUD = 2;
    public static final int FLOWER = 3;

    private static final String[] STAGE_NAMES = {"seed", "sprout", "bud", "flower"};

    private int growthStage = SEED;
    private boolean wilted = false;

    /**
     * Update the growth stage from the habit's streak.
     * Each plant type grows at its own pace, so every subclass overrides this
     * (Polymorphism).
     */
    public abstract void grow(int streak);

    /** Name of the image file for the current stage, e.g. "flower_bud.png". */
    public abstract String getPlantImage();

    /** Short name of the plant type, e.g. "flower". */
    public abstract String getTypeName();

    // ---------- helpers shared by all subclasses ----------

    protected void setGrowthStage(int stage) {
        if (stage < SEED) stage = SEED;
        if (stage > FLOWER) stage = FLOWER;
        this.growthStage = stage;
    }

    /** Builds "type_stage.png" or "type_stage_wilted.png". */
    protected String buildImageName() {
        String name = getTypeName() + "_" + STAGE_NAMES[growthStage];
        if (wilted) {
            name += "_wilted";
        }
        return name + ".png";
    }

    // ---------- getters / setters ----------

    public int getGrowthStage() {
        return growthStage;
    }

    public String getStageName() {
        return STAGE_NAMES[growthStage].toUpperCase();
    }

    public boolean isWilted() {
        return wilted;
    }

    public void setWilted(boolean wilted) {
        this.wilted = wilted;
    }
}
