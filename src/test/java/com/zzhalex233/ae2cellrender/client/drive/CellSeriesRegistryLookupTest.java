package com.zzhalex233.ae2cellrender.client.drive;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class CellSeriesRegistryLookupTest {

    @AfterEach
    void resetRegistry() {
        ForgeRegistries.ITEMS.clear();
    }

    @Test
    void returnsLowestTierItemForSameSeries() {
        register(Items.FIERY_STORAGE_CELL_1K);
        register(Items.FIERY_STORAGE_CELL_16K);

        Item canonical = CellSeriesRegistryLookup.INSTANCE.resolveCanonical(Items.FIERY_STORAGE_CELL_16K);

        assertSame(Items.FIERY_STORAGE_CELL_1K, canonical);
    }

    @Test
    void returnsSelfForItemsWithoutRecognizedTier() {
        register(Items.SOLO_STORAGE_CELL);

        Item canonical = CellSeriesRegistryLookup.INSTANCE.resolveCanonical(Items.SOLO_STORAGE_CELL);

        assertSame(Items.SOLO_STORAGE_CELL, canonical);
    }

    @Test
    void prefersOneKOver256KInSameSeries() {
        Item oneK = register("storage_cell_1k");
        Item twoFiftySixK = register("storage_cell_256k");

        Item canonical = CellSeriesRegistryLookup.INSTANCE.resolveCanonical(twoFiftySixK);

        assertSame(oneK, canonical);
    }

    @Test
    void prefersOneKOver4MbInSameSeries() {
        Item oneK = register("storage_cell_1k");
        Item fourMb = register("storage_cell_4mb");

        Item canonical = CellSeriesRegistryLookup.INSTANCE.resolveCanonical(fourMb);

        assertSame(oneK, canonical);
    }

    @Test
    void prefersLexicographicallyEarlierNameWhenCapacityMatches() {
        Item compact = register("quantum_drive_32mb");
        Item split = register("quantum_drive_32_mb");

        Item canonical = CellSeriesRegistryLookup.INSTANCE.resolveCanonical(compact);

        assertSame(split, canonical);
    }

    @Test
    void prefers4MbOver4GbWhenNoSmallerItemExists() {
        Item fourMb = register("storage_cell_4mb");
        Item fourGb = register("storage_cell_4gb");

        Item canonical = CellSeriesRegistryLookup.INSTANCE.resolveCanonical(fourGb);

        assertSame(fourMb, canonical);
    }

    @Test
    void doesNotGroupSimilarPrefixes() {
        Item creative = new Item().setRegistryName(new ResourceLocation("aeadditions", "fiery_storage_cell_creative"));

        register(Items.FIERY_STORAGE_CELL_1K);
        register(creative);

        Item canonical = CellSeriesRegistryLookup.INSTANCE.resolveCanonical(creative);

        assertSame(creative, canonical);
    }

    private void register(Item item) {
        ForgeRegistries.ITEMS.register(item);
    }

    private Item register(String path) {
        Item item = new Item().setRegistryName(new ResourceLocation("aeadditions", path));
        register(item);
        return item;
    }
}
