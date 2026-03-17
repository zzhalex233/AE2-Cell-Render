package com.zzhalex233.ae2cellrender.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DriveSlotLayoutsTest {

    @Test
    void ae2DefaultLayoutMatchesExistingSlotGeometry() {
        DriveSlotLayouts.Layout layout = DriveSlotLayouts.forId(DriveSlotLayouts.AE2_LAYOUT_ID);

        assertEquals(10, layout.slotCount());
        assertEquals(DriveCellSlotLayout.get(0).minX(), layout.slot(0).baseRect().minX(), 1.0e-6F);
        assertEquals(DriveCellSlotLayout.getRendered(0).maxY(), layout.slot(0).renderedRect().maxY(), 1.0e-6F);
    }

    @Test
    void crazyAeLayoutExposesThirtyFiveThinSlots() {
        DriveSlotLayouts.Layout layout = DriveSlotLayouts.forId(DriveSlotLayouts.CRAZYAE_LAYOUT_ID);

        assertEquals(35, layout.slotCount());
        assertEquals(14.0F / 16.0F, layout.slot(0).baseRect().minX(), 1.0e-6F);
        assertEquals(15.0F / 16.0F, layout.slot(0).baseRect().maxX(), 1.0e-6F);
        assertEquals(13.0F / 16.0F, layout.slot(0).baseRect().minY(), 1.0e-6F);
        assertEquals(2.0F / 16.0F, layout.slot(34).baseRect().minX(), 1.0e-6F);
        assertEquals(1.0F / 16.0F, layout.slot(34).baseRect().minY(), 1.0e-6F);
    }

    @Test
    void aeAdditionsLayoutExposesThreeVerticalSlots() {
        DriveSlotLayouts.Layout layout = DriveSlotLayouts.forId(DriveSlotLayouts.AEADDITIONS_LAYOUT_ID);

        assertEquals(3, layout.slotCount());
        assertEquals(5.0F / 16.0F, layout.slot(0).baseRect().minX(), 1.0e-6F);
        assertEquals(11.0F / 16.0F, layout.slot(0).baseRect().maxX(), 1.0e-6F);
        assertEquals(10.0F / 16.0F, layout.slot(0).baseRect().minY(), 1.0e-6F);
        assertEquals(4.0F / 16.0F, layout.slot(2).baseRect().minY(), 1.0e-6F);
    }

    @Test
    void unknownLayoutFallsBackToAe2Default() {
        assertSame(DriveSlotLayouts.forId(DriveSlotLayouts.AE2_LAYOUT_ID), DriveSlotLayouts.forId("missing:layout"));
    }
}
