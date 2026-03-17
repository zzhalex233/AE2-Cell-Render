package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LabColorMathTest {

    @Test
    void rgbToLabOrdersBlackAndWhiteByLightness() {
        CellColorMath.LabColor black = CellColorMath.lab(0xFF000000);
        CellColorMath.LabColor white = CellColorMath.lab(0xFFFFFFFF);

        assertTrue(black.lightness() < 5.0F);
        assertTrue(white.lightness() > 95.0F);
        assertTrue(white.lightness() > black.lightness());
    }

    @Test
    void deltaESanityKeepsWarmGrayPairCloserThanWarmCoolPair() {
        CellColorMath.LabColor warmGrayOne = CellColorMath.lab(0xFF8A7F7A);
        CellColorMath.LabColor warmGrayTwo = CellColorMath.lab(0xFF968F88);
        float warmGrayDistance = CellColorMath.deltaE(warmGrayOne, warmGrayTwo);

        CellColorMath.LabColor warm = CellColorMath.lab(0xFFE57248);
        CellColorMath.LabColor cool = CellColorMath.lab(0xFF16B7E8);
        float warmCoolDistance = CellColorMath.deltaE(warm, cool);

        assertTrue(warmGrayDistance > 0.0F);
        assertTrue(warmCoolDistance > warmGrayDistance);
    }
}
