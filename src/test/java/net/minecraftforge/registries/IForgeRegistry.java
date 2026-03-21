package net.minecraftforge.registries;

import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Set;

public interface IForgeRegistry<T> {
    void clear();

    void register(T value);

    T getValue(ResourceLocation key);

    Collection<T> getValuesCollection();

    Set<ResourceLocation> getKeys();
}