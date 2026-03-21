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
    void unknownLayoutFallsBackToAe2Default() {
        assertSame(DriveSlotLayouts.forId(DriveSlotLayouts.AE2_LAYOUT_ID), DriveSlotLayouts.forId("missing:layout"));
    }
}
