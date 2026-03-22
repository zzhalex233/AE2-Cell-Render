package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellSpriteColorAnalyzerTest {

    @Test
    void bodyBeatsBlackOutlineAndTinyBrightAccent() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithIndicatorAndOutline();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void bodyBeatsDarkContourLines() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.darkBodyWithInternalOutline();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void bodyBeatsWhiteHighlightSpecks() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithWhiteHighlightSpecks();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void bodyNearBorderStillWinsAfterRingDownweighting() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyNearBorderWithTinyAccent();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void grayBodyBeatsDarkStructureFamilies() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.grayBodyWithDarkStructureAndPurpleAccent();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.outlineColor),
                "resolved=#" + Integer.toHexString(resolved)
                        + " body=#" + Integer.toHexString(sprite.bodyColor)
                        + " outline=#" + Integer.toHexString(sprite.outlineColor)
        );
        assertCloserToBody(resolved, sprite);
    }

    @Test
    void litBodyToneBeatsSameHueShadowBand() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithShadowedLowerHalf();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void warmBodyIgnoresTinyYellowTierIndicator() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.warmBodyWithYellowTierIndicator();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void warmBodyIgnoresTinyRedTierIndicator() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.warmBodyWithRedTierIndicator();

        int resolved = CellSpriteMainColorExtractor.mainColor(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(resolved, sprite);
    }

    @Test
    void sameSeriesWarmBodyStaysStableAcrossTierIndicatorColors() {
        GeneratedCellSpriteFixtures.SpritePixels yellowTier = GeneratedCellSpriteFixtures.warmBodyWithYellowTierIndicator();
        GeneratedCellSpriteFixtures.SpritePixels redTier = GeneratedCellSpriteFixtures.warmBodyWithRedTierIndicator();

        int yellowResolved = CellSpriteMainColorExtractor.mainColor(
                yellowTier.pixels,
                yellowTier.width,
                yellowTier.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );
        int redResolved = CellSpriteMainColorExtractor.mainColor(
                redTier.pixels,
                redTier.width,
                redTier.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(yellowResolved, yellowTier);
        assertCloserToBody(redResolved, redTier);
        assertTrue(
                CellColorMath.colorDistance(yellowResolved, redResolved) <= 16.0F,
                "yellow=#" + Integer.toHexString(yellowResolved)
                        + " red=#" + Integer.toHexString(redResolved)
        );
    }

    @Test
    void fieryStorageCellTiersPreferSharedDarkShellOverBrightCore() {
        GeneratedCellSpriteFixtures.SpritePixels oneK = GeneratedCellSpriteFixtures.spriteFromResource("/com/zzhalex233/ae2cellrender/client/drive/twilightforestaddons/fiery_storage_cell_1k.png");
        GeneratedCellSpriteFixtures.SpritePixels fourK = GeneratedCellSpriteFixtures.spriteFromResource("/com/zzhalex233/ae2cellrender/client/drive/twilightforestaddons/fiery_storage_cell_4k.png");
        GeneratedCellSpriteFixtures.SpritePixels sixteenK = GeneratedCellSpriteFixtures.spriteFromResource("/com/zzhalex233/ae2cellrender/client/drive/twilightforestaddons/fiery_storage_cell_16k.png");
        GeneratedCellSpriteFixtures.SpritePixels sixtyFourK = GeneratedCellSpriteFixtures.spriteFromResource("/com/zzhalex233/ae2cellrender/client/drive/twilightforestaddons/fiery_storage_cell_64k.png");

        int shellReference = pixel(oneK, 4, 4);
        int brightCoreReference = pixel(oneK, 3, 3);

        int oneKResolved = CellSpriteMainColorExtractor.mainColor(oneK.pixels, oneK.width, oneK.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));
        int fourKResolved = CellSpriteMainColorExtractor.mainColor(fourK.pixels, fourK.width, fourK.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));
        int sixteenKResolved = CellSpriteMainColorExtractor.mainColor(sixteenK.pixels, sixteenK.width, sixteenK.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));
        int sixtyFourKResolved = CellSpriteMainColorExtractor.mainColor(sixtyFourK.pixels, sixtyFourK.width, sixtyFourK.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));

        assertCloserToShell(oneKResolved, shellReference, brightCoreReference);
        assertCloserToShell(fourKResolved, shellReference, brightCoreReference);
        assertCloserToShell(sixteenKResolved, shellReference, brightCoreReference);
        assertCloserToShell(sixtyFourKResolved, shellReference, brightCoreReference);

        assertTrue(
                CellColorMath.deltaE(CellColorMath.lab(oneKResolved), CellColorMath.lab(sixteenKResolved)) <= 12.0F,
                "1k=#" + Integer.toHexString(oneKResolved) + " 16k=#" + Integer.toHexString(sixteenKResolved)
        );
        assertTrue(
                CellColorMath.deltaE(CellColorMath.lab(fourKResolved), CellColorMath.lab(sixteenKResolved)) <= 12.0F,
                "4k=#" + Integer.toHexString(fourKResolved) + " 16k=#" + Integer.toHexString(sixteenKResolved)
        );
        assertTrue(
                CellColorMath.deltaE(CellColorMath.lab(sixtyFourKResolved), CellColorMath.lab(sixteenKResolved)) <= 6.0F,
                "64k=#" + Integer.toHexString(sixtyFourKResolved) + " 16k=#" + Integer.toHexString(sixteenKResolved)
        );
    }

    @Test
    void crazyAeMegabyteStorageCellsPreferPaleShellOverGrayBlueBody() {
        GeneratedCellSpriteFixtures.SpritePixels oneMb = GeneratedCellSpriteFixtures.spriteFromResource("/com/zzhalex233/ae2cellrender/client/drive/crazyae/storage_cell_1mb.png");
        GeneratedCellSpriteFixtures.SpritePixels fourMb = GeneratedCellSpriteFixtures.spriteFromResource("/com/zzhalex233/ae2cellrender/client/drive/crazyae/storage_cell_4mb.png");
        GeneratedCellSpriteFixtures.SpritePixels sixtyFourMb = GeneratedCellSpriteFixtures.spriteFromResource("/com/zzhalex233/ae2cellrender/client/drive/crazyae/storage_cell_64mb.png");

        int paleShellReference = pixel(oneMb, 4, 4);
        int grayBlueReference = pixel(oneMb, 3, 3);

        int oneMbResolved = CellSpriteMainColorExtractor.mainColor(oneMb.pixels, oneMb.width, oneMb.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));
        int fourMbResolved = CellSpriteMainColorExtractor.mainColor(fourMb.pixels, fourMb.width, fourMb.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));
        int sixtyFourMbResolved = CellSpriteMainColorExtractor.mainColor(sixtyFourMb.pixels, sixtyFourMb.width, sixtyFourMb.height, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF));

        assertCloserToPaleShell(oneMbResolved, paleShellReference, grayBlueReference);
        assertCloserToPaleShell(fourMbResolved, paleShellReference, grayBlueReference);
        assertCloserToPaleShell(sixtyFourMbResolved, paleShellReference, grayBlueReference);

        assertTrue(
                CellColorMath.deltaE(CellColorMath.lab(oneMbResolved), CellColorMath.lab(fourMbResolved)) <= 8.0F,
                "1mb=#" + Integer.toHexString(oneMbResolved) + " 4mb=#" + Integer.toHexString(fourMbResolved)
        );
        assertTrue(
                CellColorMath.deltaE(CellColorMath.lab(sixtyFourMbResolved), CellColorMath.lab(fourMbResolved)) <= 8.0F,
                "64mb=#" + Integer.toHexString(sixtyFourMbResolved) + " 4mb=#" + Integer.toHexString(fourMbResolved)
        );
    }

    @Test
    void candidateExposesOpaqueAndClusterSupportMetadata() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.bodyWithIndicatorAndOutline();

        CellSpriteColorCandidate candidate = CellSpriteColorAnalyzer.mainBodyCandidate(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(candidate.argb(), sprite);
        assertEquals(countOpaque(sprite.pixels), candidate.opaqueSampleCount());
        assertTrue(candidate.clusterWeight() > 0.0F, "cluster weight should be positive");
        assertTrue(candidate.familyWeight() >= candidate.clusterWeight(), "family support should include the winning cluster");
        assertTrue(candidate.innerWeightRatio() > 0.0F, "body candidate should have inner support");
        assertTrue(candidate.clusterCount() >= 1, "cluster count should be exposed");
    }

    @Test
    void candidateKeepsBodyLikeMetricsForGraySprite() {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.grayBodyWithDarkStructureAndPurpleAccent();

        CellSpriteColorCandidate candidate = CellSpriteColorAnalyzer.mainBodyCandidate(
                sprite.pixels,
                sprite.width,
                sprite.height,
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF)
        );

        assertCloserToBody(candidate.argb(), sprite);
        assertTrue(candidate.innerWeightRatio() >= 0.40F, "gray body should still look body-like");
        assertTrue(candidate.averageLightness() > 25.0F, "candidate should expose representative lightness");
    }

    private static void assertCloserToBody(int resolved, GeneratedCellSpriteFixtures.SpritePixels sprite) {
        assertTrue(
                CellColorMath.colorDistance(resolved, sprite.bodyColor)
                        < CellColorMath.colorDistance(resolved, sprite.accentColor),
                "resolved=#" + Integer.toHexString(resolved)
                        + " body=#" + Integer.toHexString(sprite.bodyColor)
                        + " accent=#" + Integer.toHexString(sprite.accentColor)
        );
    }

    private static void assertCloserToShell(int resolved, int shellReference, int brightCoreReference) {
        assertTrue(
                CellColorMath.colorDistance(resolved, shellReference)
                        < CellColorMath.colorDistance(resolved, brightCoreReference),
                "resolved=#" + Integer.toHexString(resolved)
                        + " shell=#" + Integer.toHexString(shellReference)
                        + " core=#" + Integer.toHexString(brightCoreReference)
        );
    }

    private static void assertCloserToPaleShell(int resolved, int paleShellReference, int grayBlueReference) {
        assertTrue(
                CellColorMath.colorDistance(resolved, paleShellReference)
                        < CellColorMath.colorDistance(resolved, grayBlueReference),
                "resolved=#" + Integer.toHexString(resolved)
                        + " pale=#" + Integer.toHexString(paleShellReference)
                        + " grayBlue=#" + Integer.toHexString(grayBlueReference)
        );
    }

    private static int pixel(GeneratedCellSpriteFixtures.SpritePixels sprite, int x, int y) {
        return sprite.pixels[(y * sprite.width) + x];
    }

    private static int countOpaque(int[] pixels) {
        int count = 0;
        for (int pixel : pixels) {
            if (((pixel >>> 24) & 0xFF) >= CellSpriteWeightedSamples.ALPHA_MIN) {
                count++;
            }
        }
        return count;
    }
}
