package net.minecraftforge.fml.common.registry;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ForgeRegistries {
    public static final IForgeRegistry<Item> ITEMS = new ItemRegistry();

    private ForgeRegistries() {
    }

    private static final class ItemRegistry implements IForgeRegistry<Item> {
        private final Map<ResourceLocation, Item> items = new LinkedHashMap<>();

        @Override
        public void clear() {
            items.clear();
        }

        @Override
        public void register(Item item) {
            if (item != null && item.getRegistryName() != null) {
                items.put(item.getRegistryName(), item);
            }
        }

        @Override
        public Item getValue(ResourceLocation key) {
            return items.get(key);
        }

        @Override
        public Collection<Item> getValuesCollection() {
            return Collections.unmodifiableCollection(items.values());
        }

        @Override
        public Set<ResourceLocation> getKeys() {
            return Collections.unmodifiableSet(items.keySet());
        }
    }
}