package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveCellFastRendererPowerGateTest {

    @Test
    void renderGateOnlyAllowsPoweredDrives() {
        assertFalse(DriveCellRenderGate.shouldRenderColorLayer(false));
        assertTrue(DriveCellRenderGate.shouldRenderColorLayer(true));
    }
}
