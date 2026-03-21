package com.zzhalex233.ae2cellrender.client.drive;

final class CellSpriteColorCandidate {
    private final int argb;
    private final int opaqueSampleCount;
    private final float clusterWeight;
    private final float familyWeight;
    private final float innerWeightRatio;
    private final int clusterCount;
    private final float averageSaturation;
    private final float averageLightness;

    CellSpriteColorCandidate(int argb,
                             int opaqueSampleCount,
                             float clusterWeight,
                             float familyWeight,
                             float innerWeightRatio,
                             int clusterCount,
                             float averageSaturation,
                             float averageLightness) {
        this.argb = argb;
        this.opaqueSampleCount = opaqueSampleCount;
        this.clusterWeight = clusterWeight;
        this.familyWeight = familyWeight;
        this.innerWeightRatio = innerWeightRatio;
        this.clusterCount = clusterCount;
        this.averageSaturation = averageSaturation;
        this.averageLightness = averageLightness;
    }

    int argb() {
        return argb;
    }

    int opaqueSampleCount() {
        return opaqueSampleCount;
    }

    float clusterWeight() {
        return clusterWeight;
    }

    float familyWeight() {
        return familyWeight;
    }

    float innerWeightRatio() {
        return innerWeightRatio;
    }

    int clusterCount() {
        return clusterCount;
    }

    float averageSaturation() {
        return averageSaturation;
    }

    float averageLightness() {
        return averageLightness;
    }
}
