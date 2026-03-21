package com.zzhalex233.ae2cellrender.client.drive;

public final class CellSpriteColorAnalyzer {

    private CellSpriteColorAnalyzer() {
    }

    static CellSpriteColorCandidate mainBodyCandidate(int[] pixels, int width, int height, int fallback) {
        return CellSpriteMainColorExtractor.mainColorCandidate(pixels, width, height, fallback);
    }

    public static int mainBodyColor(int[] pixels, int width, int height, int fallback) {
        return mainBodyCandidate(pixels, width, height, fallback).argb();
    }
}
