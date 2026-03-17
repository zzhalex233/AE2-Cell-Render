package com.zzhalex233.ae2cellrender.client.drive.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveMaterialRegionsTest {

    @Test
    void frontRegionMatchesOverlayFootprintAndKeepsCutoutMask() {
        DriveMaterialRegions.MaterialRegion region = DriveMaterialRegions.frontRegion();

        assertEquals(6.0F / 16.0F, region.getWidth(), 1.0e-6F);
        assertEquals(2.0F / 16.0F, region.getHeight(), 1.0e-6F);
        assertTrue(region.usesCutoutMask());
    }

    @Test
    void solidRegionsShareTheExpectedSampleWindow() {
        DriveMaterialRegions.MaterialRegion top = DriveMaterialRegions.topRegion();
        DriveMaterialRegions.MaterialRegion side = DriveMaterialRegions.sideRegion();
        DriveMaterialRegions.MaterialRegion bottom = DriveMaterialRegions.bottomRegion();

        assertEquals(2.0F / 16.0F, top.getWidth(), 1.0e-6F);
        assertEquals(1.0F / 16.0F, top.getHeight(), 1.0e-6F);
        assertFalse(top.usesCutoutMask());
        assertEquals(top.getMinU(), side.getMinU(), 1.0e-6F);
        assertEquals(top.getMaxU(), bottom.getMaxU(), 1.0e-6F);
    }
}
