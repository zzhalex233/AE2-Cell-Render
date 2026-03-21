package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveCellFastRendererShadingTest {

    @Test
    void shadeHelperAdjustsEachComponent() {
        assertEquals(255, DriveCellShading.shadeComponent(250, 1.05F));
        assertEquals(140, DriveCellShading.shadeComponent(150, 0.93F));
        assertEquals(46, DriveCellShading.shadeComponent(50, 0.92F));
    }

    @Test
    void bottomShadingStaysCloseToSideShading() {
        int side = DriveCellShading.shadeComponent(200, 0.93F);
        int bottom = DriveCellShading.shadeComponent(200, 0.92F);

        assertTrue(Math.abs(side - bottom) <= 2);
    }
}