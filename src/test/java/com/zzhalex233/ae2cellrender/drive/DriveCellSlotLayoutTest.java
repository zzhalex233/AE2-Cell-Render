package com.zzhalex233.ae2cellrender.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveCellSlotLayoutTest {

    @Test
    void frontOverlaySitsInFrontOfAe2CellBackdrop() {
        assertTrue(DriveCellSlotLayout.FRONT_Z < (1.0F / 16.0F));
    }

    @Test
    void extrudedCuboidHasRealDepth() {
        assertTrue(DriveCellSlotLayout.BACK_Z > DriveCellSlotLayout.FRONT_Z);
        assertTrue(DriveCellSlotLayout.BACK_Z < (3.0F / 16.0F));
    }

    @Test
    void slotZeroMatchesAe2TopRightCell() {
        DriveCellSlotLayout.SlotRect rect = DriveCellSlotLayout.get(0);
        assertEquals(9.0F / 16.0F, rect.minX(), 0.0001F);
        assertEquals(13.0F / 16.0F, rect.minY(), 0.0001F);
        assertEquals(15.0F / 16.0F, rect.maxX(), 0.0001F);
        assertEquals(15.0F / 16.0F, rect.maxY(), 0.0001F);
    }

    @Test
    void slotNineMatchesAe2BottomLeftCell() {
        DriveCellSlotLayout.SlotRect rect = DriveCellSlotLayout.get(9);
        assertEquals(1.0F / 16.0F, rect.minX(), 0.0001F);
        assertEquals(1.0F / 16.0F, rect.minY(), 0.0001F);
        assertEquals(7.0F / 16.0F, rect.maxX(), 0.0001F);
        assertEquals(3.0F / 16.0F, rect.maxY(), 0.0001F);
    }

    @Test
    void colorOverlayUsesTheFullSlotRect() {
        DriveCellSlotLayout.SlotRect rect = DriveCellSlotLayout.get(0);
        assertEquals(9.0F / 16.0F, rect.minX(), 0.0001F);
        assertEquals(13.0F / 16.0F, rect.minY(), 0.0001F);
        assertEquals(15.0F / 16.0F, rect.maxX(), 0.0001F);
        assertEquals(15.0F / 16.0F, rect.maxY(), 0.0001F);
    }

    @Test
    void renderedGeometryUsesCurrentInsetFrontRectangle() {
        DriveCellSlotLayout.SlotRect slot = DriveCellSlotLayout.get(0);
        DriveCellSlotLayout.SlotRect rendered = DriveCellSlotLayout.getRendered(0);

        assertEquals(slot.minX() + DriveCellSlotLayout.SIDE_BAND, rendered.minX(), 0.0001F);
        assertEquals(slot.minY() - DriveCellSlotLayout.Y_OVERDRAW, rendered.minY(), 0.0001F);
        assertEquals(slot.maxX() - DriveCellSlotLayout.SIDE_BAND, rendered.maxX(), 0.0001F);
        assertEquals(slot.maxY() - DriveCellSlotLayout.TOP_BAND + DriveCellSlotLayout.Y_OVERDRAW, rendered.maxY(), 0.0001F);
    }

    @Test
    void renderedRectangleCanFlushWithOriginalSideWalls() {
        DriveCellSlotLayout.SlotRect slot = DriveCellSlotLayout.get(0);
        DriveCellSlotLayout.SlotRect rendered = DriveCellSlotLayout.getRendered(0);

        assertTrue(rendered.minX() >= slot.minX());
        assertTrue(rendered.maxX() <= slot.maxX());
        assertTrue(rendered.minY() < slot.maxY());
        assertTrue(rendered.maxY() > slot.minY());
    }

    @Test
    void visibleModelInsetStillLooksCloseToFullSize() {
        assertEquals(0.0F, DriveCellSlotLayout.SIDE_BAND, 0.000001F);
        assertEquals(0.046875F / 16.0F, DriveCellSlotLayout.TOP_BAND, 0.000001F);
        assertTrue(DriveCellSlotLayout.Y_OVERDRAW <= (0.125F / 16.0F));
    }
}
