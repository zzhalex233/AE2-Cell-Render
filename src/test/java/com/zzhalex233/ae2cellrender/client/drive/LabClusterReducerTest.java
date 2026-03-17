package com.zzhalex233.ae2cellrender.client.drive;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LabClusterReducerTest {

    @Test
    void removesClustersBelowMinimumWeightRatio() {
        List<LabClusterReducer.Cluster> reduced = LabClusterReducer.filterAndMerge(Arrays.asList(
                cluster(0xFFE57248, 97.0F, 12),
                cluster(0xFFE86F4A, 2.0F, 1),
                cluster(0xFF16B7E8, 1.0F, 1)
        ));

        assertEquals(1, reduced.size());
        assertEquals(0xFFE57248, reduced.get(0).argb());
    }

    @Test
    void mergesNearbyClustersUnderDeltaEThreshold() {
        List<LabClusterReducer.Cluster> reduced = LabClusterReducer.filterAndMerge(Arrays.asList(
                cluster(0xFFE57248, 40.0F, 8),
                cluster(0xFFD96B45, 20.0F, 5)
        ));

        assertEquals(1, reduced.size());
        assertEquals(60.0F, reduced.get(0).totalWeight(), 0.001F);
    }

    @Test
    void keepsDistantClustersSeparate() {
        List<LabClusterReducer.Cluster> reduced = LabClusterReducer.filterAndMerge(Arrays.asList(
                cluster(0xFFE57248, 40.0F, 8),
                cluster(0xFF16B7E8, 35.0F, 6)
        ));

        assertEquals(2, reduced.size());
    }

    private static LabClusterReducer.Cluster cluster(int argb, float totalWeight, int sampleCount) {
        return new LabClusterReducer.Cluster(CellColorMath.lab(argb), argb, totalWeight, sampleCount);
    }
}
