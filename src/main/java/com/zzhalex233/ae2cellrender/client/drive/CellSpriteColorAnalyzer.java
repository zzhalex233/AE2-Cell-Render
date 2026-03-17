package com.zzhalex233.ae2cellrender.client.drive;

public final class CellSpriteColorAnalyzer {

    private CellSpriteColorAnalyzer() {
    }

    public static int mainBodyColor(int[] pixels, int width, int height, int fallback) {
        return CellSpriteMainColorExtractor.mainColor(pixels, width, height, fallback);
    }
}
