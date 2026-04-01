package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class Item {
    public enum ToolMaterial {
        IRON
    }

    private ResourceLocation registryName;
    private String translationKey;

    public Item setRegistryName(ResourceLocation registryName) {
        this.registryName = registryName;
        return this;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public Item setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
        return this;
    }

    public String getTranslationKey() {
        if (translationKey != null) {
            return translationKey;
        }
        if (registryName == null) {
            return "item.unknown";
        }
        return "item." + registryName.getNamespace() + "." + registryName.getPath();
    }

    public String getTranslationKey(ItemStack stack) {
        return getTranslationKey();
    }

    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }
}
