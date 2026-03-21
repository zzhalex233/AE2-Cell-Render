package com.zzhalex233.ae2cellrender.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class AE2CellRenderConfig {
    private static final String CATEGORY_CLIENT = "client";
    private static final String ENABLE_SERIES_COLOR_FAMILIES = "enableSeriesColorFamilies";

    private static volatile boolean enableSeriesColorFamilies = true;
    private static volatile Boolean enableSeriesColorFamiliesOverride;

    private AE2CellRenderConfig() {
    }

    public static synchronized void load(File file) {
        if (file == null) {
            enableSeriesColorFamilies = true;
            return;
        }

        Configuration configuration = new Configuration(file);
        try {
            configuration.load();
            enableSeriesColorFamilies = configuration.getBoolean(
                    ENABLE_SERIES_COLOR_FAMILIES,
                    CATEGORY_CLIENT,
                    true,
                    "If true, registry-name series items may split into multiple color families when their main colors are far apart. "
                            + "If false, each normalized series collapses back to one canonical color source."
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

    public static void overrideEnableSeriesColorFamiliesForTests(boolean enabled) {
        enableSeriesColorFamiliesOverride = enabled;
    }

    public static void resetForTests() {
        enableSeriesColorFamilies = true;
        enableSeriesColorFamiliesOverride = null;
    }
}
