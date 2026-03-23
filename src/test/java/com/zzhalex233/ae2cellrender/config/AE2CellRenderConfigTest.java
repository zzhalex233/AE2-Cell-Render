package com.zzhalex233.ae2cellrender.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AE2CellRenderConfigTest {

    @AfterEach
    void resetConfigState() {
        AE2CellRenderConfig.resetForTests();
    }

    @Test
    void defaultsMatchCurrentHardcodedBehavior() {
        AE2CellRenderConfig.resetForTests();

        assertTrue(AE2CellRenderConfig.isSeriesColorFamiliesEnabled());
        assertTrue(AE2CellRenderConfig.isPreferSameHueFamiliesEnabled());
        assertEquals(55.0F, AE2CellRenderConfig.familyHueThreshold(), 0.001F);
        assertEquals(42.0F, AE2CellRenderConfig.familyColorDistanceThreshold(), 0.001F);
        assertEquals(10.0F, AE2CellRenderConfig.familyNeutralLightnessThreshold(), 0.001F);
        assertFalse(AE2CellRenderConfig.isRawColorFilterEnabled());
        assertTrue(AE2CellRenderConfig.isDisplayColorEnhancementEnabled());
        assertEquals(1.0F, AE2CellRenderConfig.displayBrightnessBoost(), 0.001F);
        assertEquals(1.0F, AE2CellRenderConfig.displaySaturationBoost(), 0.001F);
        assertTrue(AE2CellRenderConfig.isSoftPastelPreservationEnabled());
        assertEquals(1.0F, AE2CellRenderConfig.neutralCleanlinessBoost(), 0.001F);
    }

    @Test
    void overrideHelpersCanChangeAndResetValues() {
        AE2CellRenderConfig.overrideEnableSeriesColorFamiliesForTests(false);
        AE2CellRenderConfig.overridePreferSameHueFamiliesForTests(false);
        AE2CellRenderConfig.overrideFamilyHueThresholdForTests(20.0F);
        AE2CellRenderConfig.overrideFamilyColorDistanceThresholdForTests(30.0F);
        AE2CellRenderConfig.overrideFamilyNeutralLightnessThresholdForTests(6.0F);
        AE2CellRenderConfig.overrideRawColorFilterForTests(false);
        AE2CellRenderConfig.overrideEnableDisplayColorEnhancementForTests(false);
        AE2CellRenderConfig.overrideDisplayBrightnessBoostForTests(0.8F);
        AE2CellRenderConfig.overrideDisplaySaturationBoostForTests(0.7F);
        AE2CellRenderConfig.overrideSoftPastelPreservationForTests(false);
        AE2CellRenderConfig.overrideNeutralCleanlinessBoostForTests(0.9F);

        assertFalse(AE2CellRenderConfig.isSeriesColorFamiliesEnabled());
        assertFalse(AE2CellRenderConfig.isPreferSameHueFamiliesEnabled());
        assertEquals(20.0F, AE2CellRenderConfig.familyHueThreshold(), 0.001F);
        assertEquals(30.0F, AE2CellRenderConfig.familyColorDistanceThreshold(), 0.001F);
        assertEquals(6.0F, AE2CellRenderConfig.familyNeutralLightnessThreshold(), 0.001F);
        assertFalse(AE2CellRenderConfig.isRawColorFilterEnabled());
        assertFalse(AE2CellRenderConfig.isDisplayColorEnhancementEnabled());
        assertEquals(0.8F, AE2CellRenderConfig.displayBrightnessBoost(), 0.001F);
        assertEquals(0.7F, AE2CellRenderConfig.displaySaturationBoost(), 0.001F);
        assertFalse(AE2CellRenderConfig.isSoftPastelPreservationEnabled());
        assertEquals(0.9F, AE2CellRenderConfig.neutralCleanlinessBoost(), 0.001F);

        AE2CellRenderConfig.resetForTests();

        assertTrue(AE2CellRenderConfig.isSeriesColorFamiliesEnabled());
        assertTrue(AE2CellRenderConfig.isPreferSameHueFamiliesEnabled());
        assertEquals(55.0F, AE2CellRenderConfig.familyHueThreshold(), 0.001F);
        assertEquals(42.0F, AE2CellRenderConfig.familyColorDistanceThreshold(), 0.001F);
        assertEquals(10.0F, AE2CellRenderConfig.familyNeutralLightnessThreshold(), 0.001F);
        assertFalse(AE2CellRenderConfig.isRawColorFilterEnabled());
        assertTrue(AE2CellRenderConfig.isDisplayColorEnhancementEnabled());
        assertEquals(1.0F, AE2CellRenderConfig.displayBrightnessBoost(), 0.001F);
        assertEquals(1.0F, AE2CellRenderConfig.displaySaturationBoost(), 0.001F);
        assertTrue(AE2CellRenderConfig.isSoftPastelPreservationEnabled());
        assertEquals(1.0F, AE2CellRenderConfig.neutralCleanlinessBoost(), 0.001F);
    }
}

