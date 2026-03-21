package com.zzhalex233.ae2cellrender.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class AE2CellRenderConfig {
    private static final String CATEGORY_CLIENT = "client";
    private static final String ENABLE_SERIES_COLOR_FAMILIES = "enableSeriesColorFamilies";
    private static final String PREFER_SAME_HUE_FAMILIES = "preferSameHueFamilies";
    private static final String FAMILY_HUE_THRESHOLD = "familyHueThreshold";
    private static final String FAMILY_COLOR_DISTANCE_THRESHOLD = "familyColorDistanceThreshold";
    private static final String FAMILY_NEUTRAL_LIGHTNESS_THRESHOLD = "familyNeutralLightnessThreshold";
    private static final String ENABLE_DISPLAY_COLOR_ENHANCEMENT = "enableDisplayColorEnhancement";
    private static final String DISPLAY_BRIGHTNESS_BOOST = "displayBrightnessBoost";
    private static final String DISPLAY_SATURATION_BOOST = "displaySaturationBoost";
    private static final String PRESERVE_SOFT_PASTELS = "preserveSoftPastels";
    private static final String NEUTRAL_CLEANLINESS_BOOST = "neutralCleanlinessBoost";

    private static final boolean DEFAULT_ENABLE_SERIES_COLOR_FAMILIES = true;
    private static final boolean DEFAULT_PREFER_SAME_HUE_FAMILIES = true;
    private static final float DEFAULT_FAMILY_HUE_THRESHOLD = 55.0F;
    private static final float DEFAULT_FAMILY_COLOR_DISTANCE_THRESHOLD = 42.0F;
    private static final float DEFAULT_FAMILY_NEUTRAL_LIGHTNESS_THRESHOLD = 10.0F;
    private static final boolean DEFAULT_ENABLE_DISPLAY_COLOR_ENHANCEMENT = true;
    private static final float DEFAULT_DISPLAY_BRIGHTNESS_BOOST = 1.0F;
    private static final float DEFAULT_DISPLAY_SATURATION_BOOST = 1.0F;
    private static final boolean DEFAULT_PRESERVE_SOFT_PASTELS = true;
    private static final float DEFAULT_NEUTRAL_CLEANLINESS_BOOST = 1.0F;

    private static volatile boolean enableSeriesColorFamilies = DEFAULT_ENABLE_SERIES_COLOR_FAMILIES;
    private static volatile Boolean enableSeriesColorFamiliesOverride;
    private static volatile boolean preferSameHueFamilies = DEFAULT_PREFER_SAME_HUE_FAMILIES;
    private static volatile Boolean preferSameHueFamiliesOverride;
    private static volatile float familyHueThreshold = DEFAULT_FAMILY_HUE_THRESHOLD;
    private static volatile Float familyHueThresholdOverride;
    private static volatile float familyColorDistanceThreshold = DEFAULT_FAMILY_COLOR_DISTANCE_THRESHOLD;
    private static volatile Float familyColorDistanceThresholdOverride;
    private static volatile float familyNeutralLightnessThreshold = DEFAULT_FAMILY_NEUTRAL_LIGHTNESS_THRESHOLD;
    private static volatile Float familyNeutralLightnessThresholdOverride;
    private static volatile boolean enableDisplayColorEnhancement = DEFAULT_ENABLE_DISPLAY_COLOR_ENHANCEMENT;
    private static volatile Boolean enableDisplayColorEnhancementOverride;
    private static volatile float displayBrightnessBoost = DEFAULT_DISPLAY_BRIGHTNESS_BOOST;
    private static volatile Float displayBrightnessBoostOverride;
    private static volatile float displaySaturationBoost = DEFAULT_DISPLAY_SATURATION_BOOST;
    private static volatile Float displaySaturationBoostOverride;
    private static volatile boolean preserveSoftPastels = DEFAULT_PRESERVE_SOFT_PASTELS;
    private static volatile Boolean preserveSoftPastelsOverride;
    private static volatile float neutralCleanlinessBoost = DEFAULT_NEUTRAL_CLEANLINESS_BOOST;
    private static volatile Float neutralCleanlinessBoostOverride;

    private AE2CellRenderConfig() {
    }

    public static synchronized void load(File file) {
        if (file == null) {
            // Keep the in-memory defaults aligned with the current render behavior.
            resetValuesToDefaults();
            return;
        }

        Configuration configuration = new Configuration(file);
        try {
            configuration.load();
            enableSeriesColorFamilies = configuration.getBoolean(
                    ENABLE_SERIES_COLOR_FAMILIES,
                    CATEGORY_CLIENT,
                    DEFAULT_ENABLE_SERIES_COLOR_FAMILIES,
                    "Series = one item line or metadata tier line, like storage_cell_1k/storage_cell_64k or aeadditions:storage.gas tiers. "
                            + "Family = a color subgroup inside one series. Example: turn this off if one series should keep one color."
            );
            preferSameHueFamilies = configuration.getBoolean(
                    PREFER_SAME_HUE_FAMILIES,
                    CATEGORY_CLIENT,
                    DEFAULT_PREFER_SAME_HUE_FAMILIES,
                    "If true, same-hue shades stay together more often. Example: keep this on so light blue and blue stay together."
            );
            familyHueThreshold = configuration.getFloat(
                    FAMILY_HUE_THRESHOLD,
                    CATEGORY_CLIENT,
                    DEFAULT_FAMILY_HUE_THRESHOLD,
                    0.0F,
                    180.0F,
                    "How far the color direction can move before a family splits. Example: lower splits blue and teal sooner; higher keeps them together."
            );
            familyColorDistanceThreshold = configuration.getFloat(
                    FAMILY_COLOR_DISTANCE_THRESHOLD,
                    CATEGORY_CLIENT,
                    DEFAULT_FAMILY_COLOR_DISTANCE_THRESHOLD,
                    0.0F,
                    200.0F,
                    "How different the whole color can look before a family splits. Example: lower splits light and dark shades sooner; higher keeps more shades together."
            );
            familyNeutralLightnessThreshold = configuration.getFloat(
                    FAMILY_NEUTRAL_LIGHTNESS_THRESHOLD,
                    CATEGORY_CLIENT,
                    DEFAULT_FAMILY_NEUTRAL_LIGHTNESS_THRESHOLD,
                    0.0F,
                    100.0F,
                    "How much gray, white, and pale shades may differ in brightness. Example: lower splits gray and white sooner; higher keeps neutrals together."
            );
            enableDisplayColorEnhancement = configuration.getBoolean(
                    ENABLE_DISPLAY_COLOR_ENHANCEMENT,
                    CATEGORY_CLIENT,
                    DEFAULT_ENABLE_DISPLAY_COLOR_ENHANCEMENT,
                    "If true, the final shown color gets a cleanup pass after extraction. Example: turn this off for rawer colors."
            );
            displayBrightnessBoost = configuration.getFloat(
                    DISPLAY_BRIGHTNESS_BOOST,
                    CATEGORY_CLIENT,
                    DEFAULT_DISPLAY_BRIGHTNESS_BOOST,
                    0.0F,
                    3.0F,
                    "Final color brightness. Example: 0.8 is softer, 1.2 is brighter."
            );
            displaySaturationBoost = configuration.getFloat(
                    DISPLAY_SATURATION_BOOST,
                    CATEGORY_CLIENT,
                    DEFAULT_DISPLAY_SATURATION_BOOST,
                    0.0F,
                    3.0F,
                    "Final color saturation. Example: 0.8 is softer, 1.2 is richer."
            );
            preserveSoftPastels = configuration.getBoolean(
                    PRESERVE_SOFT_PASTELS,
                    CATEGORY_CLIENT,
                    DEFAULT_PRESERVE_SOFT_PASTELS,
                    "If true, pale colors stay soft instead of turning into strong base colors. Example: keep this on so pale pink stays pale."
            );
            neutralCleanlinessBoost = configuration.getFloat(
                    NEUTRAL_CLEANLINESS_BOOST,
                    CATEGORY_CLIENT,
                    DEFAULT_NEUTRAL_CLEANLINESS_BOOST,
                    0.0F,
                    3.0F,
                    "How strongly gray and white colors get cleaned up. Example: 0.8 keeps more raw gray, 1.2 looks cleaner."
            );
        } finally {
            if (configuration.hasChanged()) {
                configuration.save();
            }
        }
    }

    public static boolean isSeriesColorFamiliesEnabled() {
        Boolean override = enableSeriesColorFamiliesOverride;
        return override != null ? override.booleanValue() : enableSeriesColorFamilies;
    }

    public static boolean isPreferSameHueFamiliesEnabled() {
        Boolean override = preferSameHueFamiliesOverride;
        return override != null ? override.booleanValue() : preferSameHueFamilies;
    }

    public static float familyHueThreshold() {
        Float override = familyHueThresholdOverride;
        return override != null ? override.floatValue() : familyHueThreshold;
    }

    public static float familyColorDistanceThreshold() {
        Float override = familyColorDistanceThresholdOverride;
        return override != null ? override.floatValue() : familyColorDistanceThreshold;
    }

    public static float familyNeutralLightnessThreshold() {
        Float override = familyNeutralLightnessThresholdOverride;
        return override != null ? override.floatValue() : familyNeutralLightnessThreshold;
    }

    public static boolean isDisplayColorEnhancementEnabled() {
        Boolean override = enableDisplayColorEnhancementOverride;
        return override != null ? override.booleanValue() : enableDisplayColorEnhancement;
    }

    public static float displayBrightnessBoost() {
        Float override = displayBrightnessBoostOverride;
        return override != null ? override.floatValue() : displayBrightnessBoost;
    }

    public static float displaySaturationBoost() {
        Float override = displaySaturationBoostOverride;
        return override != null ? override.floatValue() : displaySaturationBoost;
    }

    public static boolean isSoftPastelPreservationEnabled() {
        Boolean override = preserveSoftPastelsOverride;
        return override != null ? override.booleanValue() : preserveSoftPastels;
    }

    public static float neutralCleanlinessBoost() {
        Float override = neutralCleanlinessBoostOverride;
        return override != null ? override.floatValue() : neutralCleanlinessBoost;
    }

    public static void overrideEnableSeriesColorFamiliesForTests(boolean enabled) {
        enableSeriesColorFamiliesOverride = enabled;
    }

    public static void overridePreferSameHueFamiliesForTests(boolean enabled) {
        preferSameHueFamiliesOverride = enabled;
    }

    public static void overrideFamilyHueThresholdForTests(float value) {
        familyHueThresholdOverride = value;
    }

    public static void overrideFamilyColorDistanceThresholdForTests(float value) {
        familyColorDistanceThresholdOverride = value;
    }

    public static void overrideFamilyNeutralLightnessThresholdForTests(float value) {
        familyNeutralLightnessThresholdOverride = value;
    }

    public static void overrideEnableDisplayColorEnhancementForTests(boolean enabled) {
        enableDisplayColorEnhancementOverride = enabled;
    }

    public static void overrideDisplayBrightnessBoostForTests(float value) {
        displayBrightnessBoostOverride = value;
    }

    public static void overrideDisplaySaturationBoostForTests(float value) {
        displaySaturationBoostOverride = value;
    }

    public static void overrideSoftPastelPreservationForTests(boolean enabled) {
        preserveSoftPastelsOverride = enabled;
    }

    public static void overrideNeutralCleanlinessBoostForTests(float value) {
        neutralCleanlinessBoostOverride = value;
    }

    public static void resetForTests() {
        // Tests clear overrides so each case starts from the same baseline.
        resetValuesToDefaults();
        enableSeriesColorFamiliesOverride = null;
        preferSameHueFamiliesOverride = null;
        familyHueThresholdOverride = null;
        familyColorDistanceThresholdOverride = null;
        familyNeutralLightnessThresholdOverride = null;
        enableDisplayColorEnhancementOverride = null;
        displayBrightnessBoostOverride = null;
        displaySaturationBoostOverride = null;
        preserveSoftPastelsOverride = null;
        neutralCleanlinessBoostOverride = null;
    }

    private static void resetValuesToDefaults() {
        // These are the hardcoded values the current code path already uses.
        enableSeriesColorFamilies = DEFAULT_ENABLE_SERIES_COLOR_FAMILIES;
        preferSameHueFamilies = DEFAULT_PREFER_SAME_HUE_FAMILIES;
        familyHueThreshold = DEFAULT_FAMILY_HUE_THRESHOLD;
        familyColorDistanceThreshold = DEFAULT_FAMILY_COLOR_DISTANCE_THRESHOLD;
        familyNeutralLightnessThreshold = DEFAULT_FAMILY_NEUTRAL_LIGHTNESS_THRESHOLD;
        enableDisplayColorEnhancement = DEFAULT_ENABLE_DISPLAY_COLOR_ENHANCEMENT;
        displayBrightnessBoost = DEFAULT_DISPLAY_BRIGHTNESS_BOOST;
        displaySaturationBoost = DEFAULT_DISPLAY_SATURATION_BOOST;
        preserveSoftPastels = DEFAULT_PRESERVE_SOFT_PASTELS;
        neutralCleanlinessBoost = DEFAULT_NEUTRAL_CLEANLINESS_BOOST;
    }
}
