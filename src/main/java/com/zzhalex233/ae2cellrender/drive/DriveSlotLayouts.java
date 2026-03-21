package com.zzhalex233.ae2cellrender.drive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DriveSlotLayouts {

    public static final String AE2_LAYOUT_ID = "appliedenergistics2:drive";

    private static final Layout AE2 = new Layout(AE2_LAYOUT_ID, buildAe2Slots());

    private DriveSlotLayouts() {
    }

    public static Layout forId(String layoutId) {
        return AE2;
    }

    private static List<Slot> buildAe2Slots() {
        List<Slot> slots = new ArrayList<>(DriveCellSlotLayout.SLOT_COUNT);
        for (int slot = 0; slot < DriveCellSlotLayout.SLOT_COUNT; slot++) {
            int row = slot / 2;
            int column = slot % 2;
            float baseMinX = (column == 0 ? 9.0F : 1.0F) / 16.0F;
            float baseMaxX = baseMinX + (6.0F / 16.0F);
            float baseMinY = (13.0F - (row * 3.0F)) / 16.0F;
            float baseMaxY = baseMinY + (2.0F / 16.0F);
            DriveCellSlotLayout.SlotRect baseRect = new DriveCellSlotLayout.SlotRect(baseMinX, baseMinY, baseMaxX, baseMaxY);
            DriveCellSlotLayout.SlotRect renderedRect = new DriveCellSlotLayout.SlotRect(
                    baseMinX + DriveCellSlotLayout.SIDE_BAND,
                    baseMinY - DriveCellSlotLayout.Y_OVERDRAW,
                    baseMaxX - DriveCellSlotLayout.SIDE_BAND,
                    baseMaxY - DriveCellSlotLayout.TOP_BAND + DriveCellSlotLayout.Y_OVERDRAW
            );
            slots.add(new Slot(baseRect, renderedRect, DriveCellSlotLayout.FRONT_Z, DriveCellSlotLayout.BACK_Z));
        }
        return slots;
    }

    public static final class Layout {
        private final String id;
        private final List<Slot> slots;
        private final float frontZ;
        private final float backZ;

        private Layout(String id, List<Slot> slots) {
            this.id = id;
            this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
            this.frontZ = slots.get(0).frontZ();
            this.backZ = slots.get(0).backZ();
        }

        public String id() {
            return id;
        }

        public int slotCount() {
            return slots.size();
        }

        public Slot slot(int index) {
            return slots.get(index);
        }

        public float frontZ() {
            return frontZ;
        }

        public float backZ() {
            return backZ;
        }
    }

    public static final class Slot {
        private final DriveCellSlotLayout.SlotRect baseRect;
        private final DriveCellSlotLayout.SlotRect renderedRect;
        private final float frontZ;
        private final float backZ;

        private Slot(DriveCellSlotLayout.SlotRect baseRect, DriveCellSlotLayout.SlotRect renderedRect, float frontZ, float backZ) {
            this.baseRect = baseRect;
            this.renderedRect = renderedRect;
            this.frontZ = frontZ;
            this.backZ = backZ;
        }

        public DriveCellSlotLayout.SlotRect baseRect() {
            return baseRect;
        }

        public DriveCellSlotLayout.SlotRect renderedRect() {
            return renderedRect;
        }

        public float frontZ() {
            return frontZ;
        }

        public float backZ() {
            return backZ;
        }
    }
}
