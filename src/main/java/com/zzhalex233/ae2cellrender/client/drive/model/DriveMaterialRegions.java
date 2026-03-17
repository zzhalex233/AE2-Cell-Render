package com.zzhalex233.ae2cellrender.client.drive.model;

public final class DriveMaterialRegions {

    private static final float FRONT_MIN_U = 0.0F / 16.0F;
    private static final float FRONT_MAX_U = 6.0F / 16.0F;
    private static final float FRONT_MIN_V = 0.0F / 16.0F;
    private static final float FRONT_MAX_V = 2.0F / 16.0F;
    private static final float SOLID_MIN_U = 8.0F / 16.0F;
    private static final float SOLID_MAX_U = 10.0F / 16.0F;
    private static final float SOLID_MIN_V = 8.0F / 16.0F;
    private static final float SOLID_MAX_V = 9.0F / 16.0F;

    private DriveMaterialRegions() {
    }

    public static MaterialRegion frontRegion() {
        return new MaterialRegion(FRONT_MIN_U, FRONT_MAX_U, FRONT_MIN_V, FRONT_MAX_V, true);
    }

    public static MaterialRegion topRegion() {
        return solidRegion();
    }

    public static MaterialRegion sideRegion() {
        return solidRegion();
    }

    public static MaterialRegion bottomRegion() {
        return solidRegion();
    }

    private static MaterialRegion solidRegion() {
        return new MaterialRegion(SOLID_MIN_U, SOLID_MAX_U, SOLID_MIN_V, SOLID_MAX_V, false);
    }

    public static final class MaterialRegion {
        private final float minU;
        private final float maxU;
        private final float minV;
        private final float maxV;
        private final boolean usesCutoutMask;

        public MaterialRegion(float minU, float maxU, float minV, float maxV, boolean usesCutoutMask) {
            this.minU = minU;
            this.maxU = maxU;
            this.minV = minV;
            this.maxV = maxV;
            this.usesCutoutMask = usesCutoutMask;
        }

        public float getMinU() {
            return minU;
        }

        public float getMaxU() {
            return maxU;
        }

        public float getMinV() {
            return minV;
        }

        public float getMaxV() {
            return maxV;
        }

        public float getWidth() {
            return maxU - minU;
        }

        public float getHeight() {
            return maxV - minV;
        }

        public boolean usesCutoutMask() {
            return usesCutoutMask;
        }
    }
}
