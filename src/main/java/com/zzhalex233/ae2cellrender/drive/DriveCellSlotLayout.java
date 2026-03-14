package com.zzhalex233.ae2cellrender.drive;

public final class DriveCellSlotLayout {

    public static final int SLOT_COUNT = 10;
    public static final float FRONT_Z = 0.99F / 16.0F;
    public static final float BACK_Z = 1.90F / 16.0F;
    public static final float SIDE_BAND = 0.0F;
    public static final float TOP_BAND = 0.046875F / 16.0F;
    public static final float Y_OVERDRAW = 0.0625F / 16.0F;

    private DriveCellSlotLayout() {
    }

    public static SlotRect get(int slot) {
        int row = slot / 2;
        int column = slot % 2;
        float minX = (column == 0 ? 9.0F : 1.0F) / 16.0F;
        float maxX = minX + (6.0F / 16.0F);
        float minY = (13.0F - (row * 3.0F)) / 16.0F;
        float maxY = minY + (2.0F / 16.0F);
        return new SlotRect(minX, minY, maxX, maxY);
    }

    public static SlotRect getRendered(int slot) {
        SlotRect slotRect = get(slot);
        return new SlotRect(
                slotRect.minX + SIDE_BAND,
                slotRect.minY - Y_OVERDRAW,
                slotRect.maxX - SIDE_BAND,
                slotRect.maxY - TOP_BAND + Y_OVERDRAW
        );
    }

    public static final class SlotRect {
        private final float minX;
        private final float minY;
        private final float maxX;
        private final float maxY;

        public SlotRect(float minX, float minY, float maxX, float maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public float minX() {
            return minX;
        }

        public float minY() {
            return minY;
        }

        public float maxX() {
            return maxX;
        }

        public float maxY() {
            return maxY;
        }
    }
}
