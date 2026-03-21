package com.zzhalex233.ae2cellrender.client.drive;

import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellSeriesKeyTest {

    @Test
    void groupsFieryStorageCell1kByBaseName() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "fiery_storage_cell_1k"));

        assertEquals("aeadditions", key.domain());
        assertEquals("fiery_storage_cell_1k", key.originalPath());
        assertEquals("fiery_storage_cell", key.normalizedPath());
        assertEquals(1, key.tierRank());
        assertTrue(key.isGrouped());
    }

    @Test
    void groupsStorageCell256kBySameBaseName() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "storage_cell_256k"));

        assertEquals("aeadditions", key.domain());
        assertEquals("storage_cell_256k", key.originalPath());
        assertEquals("storage_cell", key.normalizedPath());
        assertEquals(256, key.tierRank());
        assertTrue(key.isGrouped());
    }

    @Test
    void groupsStorageCell4MbBySameBaseName() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "storage_cell_4mb"));

        assertEquals("aeadditions", key.domain());
        assertEquals("storage_cell_4mb", key.originalPath());
        assertEquals("storage_cell", key.normalizedPath());
        assertEquals(4, key.tierRank());
        assertTrue(key.isGrouped());
    }

    @Test
    void groupsStorageCell4MixedCaseUnitsBySameBaseName() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "storage_cell_4Mb"));

        assertEquals("aeadditions", key.domain());
        assertEquals("storage_cell_4Mb", key.originalPath());
        assertEquals("storage_cell", key.normalizedPath());
        assertEquals(4, key.tierRank());
        assertTrue(key.isGrouped());
    }

    @Test
    void groupsStorageCell4GbBySameBaseName() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "storage_cell_4gb"));

        assertEquals("aeadditions", key.domain());
        assertEquals("storage_cell_4gb", key.originalPath());
        assertEquals("storage_cell", key.normalizedPath());
        assertEquals(4, key.tierRank());
        assertTrue(key.isGrouped());
    }

    @Test
    void groupsQuantumDrive32MbBySameBaseName() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "quantum_drive_32mb"));

        assertEquals("aeadditions", key.domain());
        assertEquals("quantum_drive_32mb", key.originalPath());
        assertEquals("quantum_drive", key.normalizedPath());
        assertEquals(32, key.tierRank());
        assertTrue(key.isGrouped());
    }

    @Test
    void groupsQuantumDrive32MbSplitTokenBySameBaseName() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "quantum_drive_32_mb"));

        assertEquals("aeadditions", key.domain());
        assertEquals("quantum_drive_32_mb", key.originalPath());
        assertEquals("quantum_drive", key.normalizedPath());
        assertEquals(32, key.tierRank());
        assertTrue(key.isGrouped());
    }

    @Test
    void leavesPlainStorageCellUngrouped() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "plain_storage_cell"));

        assertEquals("aeadditions", key.domain());
        assertEquals("plain_storage_cell", key.originalPath());
        assertEquals("plain_storage_cell", key.normalizedPath());
        assertEquals(Integer.MAX_VALUE, key.tierRank());
        assertFalse(key.isGrouped());
    }

    @Test
    void ignoresNonTierSuffixes() {
        CellSeriesKey key = CellSeriesKey.from(new ResourceLocation("aeadditions", "fiery_storage_cell_creative"));

        assertEquals("aeadditions", key.domain());
        assertEquals("fiery_storage_cell_creative", key.originalPath());
        assertEquals("fiery_storage_cell_creative", key.normalizedPath());
        assertEquals(Integer.MAX_VALUE, key.tierRank());
        assertFalse(key.isGrouped());
    }
}
