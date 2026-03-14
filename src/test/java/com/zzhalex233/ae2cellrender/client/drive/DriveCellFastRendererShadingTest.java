package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DriveCellFastRendererShadingTest {

    @Test
    void shadeHelperAdjustsEachComponent() {
        assertEquals(255, DriveCellShading.shadeComponent(250, 1.05F));
        assertEquals(140, DriveCellShading.shadeComponent(150, 0.93F));
        assertEquals(43, DriveCellShading.shadeComponent(50, 0.86F));
    }
}
