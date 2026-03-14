package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
