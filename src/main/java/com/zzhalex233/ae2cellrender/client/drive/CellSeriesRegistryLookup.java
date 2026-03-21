package com.zzhalex233.ae2cellrender.client.drive;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class CellSeriesRegistryLookup {
    public static final CellSeriesRegistryLookup INSTANCE = new CellSeriesRegistryLookup();

    private CellSeriesRegistryLookup() {
    }

    public static Item findCanonicalItem(Item item) {
        return INSTANCE.resolveCanonical(item);
    }

    Item resolveCanonical(Item item) {
        if (item == null) {
            return null;
        }

        ResourceLocation registryName = item.getRegistryName();
        if (registryName == null) {
            return item;
        }

        CellSeriesKey currentKey = CellSeriesKey.from(registryName);
        if (!currentKey.isGrouped()) {
            return item;
        }

        Item best = item;
        CellSeriesKey bestKey = currentKey;
        for (Item candidate : ForgeRegistries.ITEMS.getValuesCollection()) {
            ResourceLocation candidateName = candidate == null ? null : candidate.getRegistryName();
            if (candidateName == null) {
                continue;
            }

            CellSeriesKey candidateKey = CellSeriesKey.from(candidateName);
            if (!candidateKey.isGrouped()) {
                continue;
            }
            if (!currentKey.domain().equals(candidateKey.domain())) {
                continue;
            }
            if (!currentKey.normalizedPath().equals(candidateKey.normalizedPath())) {
                continue;
            }

            int capacityComparison = candidateKey.capacityKey().compareTo(bestKey.capacityKey());
            if (capacityComparison < 0 || (capacityComparison == 0 && isSameTierButEarlier(candidateName, best))) {
                best = candidate;
                bestKey = candidateKey;
            }
        }

        return best;
    }

    private boolean isSameTierButEarlier(ResourceLocation candidateName, Item best) {
        if (best == null || best.getRegistryName() == null) {
            return true;
        }
        return candidateName.toString().compareTo(best.getRegistryName().toString()) < 0;
    }
}
