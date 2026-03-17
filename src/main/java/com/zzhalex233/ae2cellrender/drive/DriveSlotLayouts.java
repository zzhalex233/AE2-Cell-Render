package com.zzhalex233.ae2cellrender.drive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DriveSlotLayouts {

    public static final String AE2_LAYOUT_ID = "appliedenergistics2:drive";
    public static final String CRAZYAE_LAYOUT_ID = "crazyae:improved_drive";
    public static final String AEADDITIONS_LAYOUT_ID = "aeadditions:hardmedrive";

    private static final Layout AE2 = new Layout(AE2_LAYOUT_ID, buildAe2Slots());
    private static final Layout CRAZYAE = new Layout(CRAZYAE_LAYOUT_ID, buildCrazyAeSlots());
    private static final Layout AEADDITIONS = new Layout(AEADDITIONS_LAYOUT_ID, buildAeAdditionsSlots());

    private DriveSlotLayouts() {
    }

    public static Layout forId(String layoutId) {
        if (CRAZYAE_LAYOUT_ID.equals(layoutId)) {
            return CRAZYAE;
        }
        if (AEADDITIONS_LAYOUT_ID.equals(layoutId)) {
            return AEADDITIONS;
        }
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

    private static List<Slot> buildCrazyAeSlots() {
        List<Slot> slots = new ArrayList<>(35);
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 7; column++) {
                float minX = (14.0F - (column * 2.0F)) / 16.0F;
                float maxX = (15.0F - (column * 2.0F)) / 16.0F;
                float minY = (13.0F - (row * 3.0F)) / 16.0F;
                float maxY = minY + (2.0F / 16.0F);
                DriveCellSlotLayout.SlotRect rect = new DriveCellSlotLayout.SlotRect(minX, minY, maxX, maxY);
                slots.add(new Slot(rect, rect, 1.0F / 16.0F, 2.0F / 16.0F));
            }
        }
        return slots;
    }

    private static List<Slot> buildAeAdditionsSlots() {
        List<Slot> slots = new ArrayList<>(3);
        for (int row = 0; row < 3; row++) {
            float minY = (10.0F - (row * 3.0F)) / 16.0F;
            float maxY = minY + (2.0F / 16.0F);
            DriveCellSlotLayout.SlotRect rect = new DriveCellSlotLayout.SlotRect(
                    5.0F / 16.0F,
                    minY,
                    11.0F / 16.0F,
                    maxY
            );
            slots.add(new Slot(rect, rect, 0.0F, 1.0F / 16.0F));
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
