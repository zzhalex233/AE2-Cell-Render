package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellColorMathTest {

    @Test
    void ignoresTransparentPixels() {
        int[] pixels = {
                GeneratedCellSpriteFixtures.transparent(),
                GeneratedCellSpriteFixtures.opaque(0xFF, 0x00, 0x00),
                GeneratedCellSpriteFixtures.opaque(0xFF, 0x00, 0x00)
        };

        assertEquals(
                GeneratedCellSpriteFixtures.opaque(0xFF, 0x00, 0x00),
                CellColorMath.averageOpaqueColor(pixels, GeneratedCellSpriteFixtures.opaque(0xFF, 0xFF, 0xFF))
        );
    }

    @Test
    void fallsBackWhenNoOpaquePixelsExist() {
        int[] pixels = {
                GeneratedCellSpriteFixtures.transparent(),
                GeneratedCellSpriteFixtures.argb(0x00, 0x11, 0x22, 0x33)
        };

        assertEquals(
                GeneratedCellSpriteFixtures.opaque(0x33, 0x66, 0x99),
                CellColorMath.averageOpaqueColor(pixels, GeneratedCellSpriteFixtures.opaque(0x33, 0x66, 0x99))
        );
    }

    @Test
    void fallsBackWhenTintIsMissingOrPureBlack() {
        assertEquals(
                GeneratedCellSpriteFixtures.opaque(0x33, 0x66, 0x99),
                CellColorMath.opaqueTintOr(-1, GeneratedCellSpriteFixtures.opaque(0x33, 0x66, 0x99))
        );
        assertEquals(
                GeneratedCellSpriteFixtures.opaque(0x33, 0x66, 0x99),
                CellColorMath.opaqueTintOr(0x000000, GeneratedCellSpriteFixtures.opaque(0x33, 0x66, 0x99))
        );
    }

    @Test
    void keepsUsableTintAsOpaqueRgb() {
        assertEquals(
                GeneratedCellSpriteFixtures.opaque(0x2C, 0x6B, 0xFF),
                CellColorMath.opaqueTintOr(0x2C6BFF, GeneratedCellSpriteFixtures.opaque(0x33, 0x66, 0x99))
        );
    }

    @Test
    void firstUsableTintReturnsFallbackWhenModelHasNoTintedQuad() {
        AtomicInteger calls = new AtomicInteger();
        IntUnaryOperator tintLookup = tintIndex -> {
            calls.incrementAndGet();
            return 0xFF7D1B03;
        };

        assertEquals(
                CellColorResolver.NO_COLOR,
                CellColorMath.firstUsableTint(new int[0], tintLookup, CellColorResolver.NO_COLOR)
        );
        assertEquals(0, calls.get());
    }

    @Test
    void firstUsableTintSkipsMissingAndBlackTints() {
        IntUnaryOperator tintLookup = tintIndex -> {
            if (tintIndex == 2) {
                return -1;
            }
            if (tintIndex == 0) {
                return 0x000000;
            }
            return 0xFFE57248;
        };

        assertEquals(
                0xFFE57248,
                CellColorMath.firstUsableTint(new int[]{2, 0, 1}, tintLookup, CellColorResolver.NO_COLOR)
        );
    }

    @Test
    void postProcessMainColorLiftsDarkNeutralTowardCleanLightNeutral() {
        int input = 0xFF6A6362;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outHsv = CellColorMath.hsv(output);

        assertTrue(outHsv.value() > inHsv.value() + 0.25F);
        assertTrue(outHsv.saturation() <= inHsv.saturation() + 0.05F);
        assertTrue(maxChannelSpread(output) <= maxChannelSpread(input));
    }

    @Test
    void postProcessMainColorKeepsCoolGreyDirectionWhileBrightening() {
        int input = 0xFF5A5E6C;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outHsv = CellColorMath.hsv(output);

        assertTrue(outHsv.value() > inHsv.value() + 0.25F);
        assertTrue(((output) & 0xFF) >= ((output >>> 16) & 0xFF));
        assertTrue(((output >>> 8) & 0xFF) <= (output & 0xFF));
    }

    @Test
    void postProcessMainColorKeepsWarmDirectionWithoutTurningDeadGrey() {
        int input = 0xFF7D524F;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outHsv = CellColorMath.hsv(output);

        assertTrue(outHsv.value() > inHsv.value() + 0.20F);
        assertTrue(((output >>> 16) & 0xFF) >= ((output >>> 8) & 0xFF));
        assertTrue(((output >>> 8) & 0xFF) >= (output & 0xFF));
        assertTrue(outHsv.saturation() >= inHsv.saturation() * 0.6F);
    }

    @Test
    void postProcessMainColorKeepsPaleWarmTonesSoftWhileBrightening() {
        int input = 0xFFD7B0AC;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outHsv = CellColorMath.hsv(output);

        assertTrue(outHsv.value() > inHsv.value());
        assertTrue(CellColorMath.hueDistance(inHsv.hue(), outHsv.hue()) < 8.0F);
        assertTrue(outHsv.saturation() <= inHsv.saturation() + 0.01F);
    }

    @Test
    void postProcessMainColorPushesGrayWhiteCloserToBrightWhiteThanTheFirstStage() {
        int input = 0xFFD7D4CF;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inputHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outputHsv = CellColorMath.hsv(output);

        assertTrue(outputHsv.value() > inputHsv.value() + 0.03F);
        assertTrue(outputHsv.saturation() <= inputHsv.saturation());
        assertTrue(maxChannelSpread(output) < maxChannelSpread(input));
        assertTrue(CellColorMath.colorDistance(output, 0xFFFFFFFF) < CellColorMath.colorDistance(input, 0xFFFFFFFF));
    }

    @Test
    void postProcessMainColorCleansLightGrayMoreAggressivelyThanTheFirstStage() {
        int input = 0xFFBBB7B0;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inputHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outputHsv = CellColorMath.hsv(output);

        assertTrue(outputHsv.value() > inputHsv.value());
        assertTrue(outputHsv.saturation() <= inputHsv.saturation());
        assertTrue(maxChannelSpread(output) <= maxChannelSpread(input));
    }

    @Test
    void postProcessMainColorMakesMutedDarkOrangeBrighterAndMoreOrangeThanTheFirstStage() {
        int input = 0xFF7F4D2E;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inputHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outputHsv = CellColorMath.hsv(output);

        assertTrue(outputHsv.value() > inputHsv.value() + 0.10F);
        assertTrue(outputHsv.saturation() > inputHsv.saturation() + 0.08F);
        assertTrue(CellColorMath.hueDistance(outputHsv.hue(), 30.0F) < CellColorMath.hueDistance(inputHsv.hue(), 30.0F));
    }

    @Test
    void postProcessMainColorKeepsNearbyMutedWarmColorsContinuous() {
        int left = argbFromHsv(25.0F, 0.113F, 0.52F);
        int right = argbFromHsv(25.0F, 0.114F, 0.52F);

        int leftOutput = CellColorMath.postProcessMainColor(left);
        int rightOutput = CellColorMath.postProcessMainColor(right);

        assertTrue(CellColorMath.colorDistance(leftOutput, rightOutput) < 1200);
        assertTrue(Math.abs(CellColorMath.hsv(leftOutput).saturation() - CellColorMath.hsv(rightOutput).saturation()) < 0.10F);
    }

    @Test
    void postProcessMainColorKeepsHueEnhancementContinuousAcrossWarmAnchorBoundary() {
        int left = argbFromHsv(45.0F, 0.50F, 0.58F);
        int right = argbFromHsv(45.1F, 0.50F, 0.58F);

        int leftOutput = CellColorMath.postProcessMainColor(left);
        int rightOutput = CellColorMath.postProcessMainColor(right);

        assertTrue(CellColorMath.colorDistance(leftOutput, rightOutput) < 120);
        assertTrue(CellColorMath.hueDistance(CellColorMath.hsv(leftOutput).hue(), CellColorMath.hsv(rightOutput).hue()) < 6.0F);
    }

    @Test
    void postProcessMainColorKeepsDustyRoseSoftWhileCleaningIt() {
        int input = 0xFFC8A0B4;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inputHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outputHsv = CellColorMath.hsv(output);

        assertTrue(outputHsv.value() > inputHsv.value() + 0.10F);
        assertTrue(outputHsv.saturation() <= inputHsv.saturation() + 0.01F);
        assertTrue(CellColorMath.hueDistance(inputHsv.hue(), outputHsv.hue()) < 6.0F);
    }

    @Test
    void postProcessMainColorKeepsDustyBlueSoftWhileCleaningIt() {
        int input = 0xFF90A5BA;
        int output = CellColorMath.postProcessMainColor(input);

        CellColorMath.HsvColor inputHsv = CellColorMath.hsv(input);
        CellColorMath.HsvColor outputHsv = CellColorMath.hsv(output);

        assertTrue(outputHsv.value() > inputHsv.value() + 0.10F);
        assertTrue(outputHsv.saturation() <= inputHsv.saturation() + 0.01F);
        assertTrue(CellColorMath.hueDistance(inputHsv.hue(), outputHsv.hue()) < 6.0F);
    }

    @Test
    void postProcessMainColorPreservesLayeringBetweenWhiteAndGrays() {
        CellColorMath.HsvColor white = CellColorMath.hsv(CellColorMath.postProcessMainColor(0xFFFFFFFF));
        CellColorMath.HsvColor lightGray = CellColorMath.hsv(CellColorMath.postProcessMainColor(0xFFD0D0D0));
        CellColorMath.HsvColor gray = CellColorMath.hsv(CellColorMath.postProcessMainColor(0xFFB8B8B8));
        CellColorMath.HsvColor darkGray = CellColorMath.hsv(CellColorMath.postProcessMainColor(0xFF969696));

        assertTrue(white.value() > lightGray.value() + 0.02F);
        assertTrue(lightGray.value() > gray.value() + 0.02F);
        assertTrue(gray.value() > darkGray.value() + 0.03F);
        assertTrue(darkGray.value() < 0.92F);
    }

    @Test
    void postProcessMainColorKeepsTintedLightGrayDistinctFromWhite() {
        int output = CellColorMath.postProcessMainColor(0xFFB7C2CE);

        assertTrue(CellColorMath.colorDistance(output, 0xFFFFFFFF) > 600);
    }

    private int maxChannelSpread(int color) {
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        return Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue));
    }

    private int argbFromHsv(float hue, float saturation, float value) {
        int rgb = Color.HSBtoRGB(hue / 360.0F, saturation, value);
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }
}
