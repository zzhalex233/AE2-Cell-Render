package mods.flammpfeil.slashblade;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.item.ItemStack;

public class SlashBlade {
    public static ItemSlashBladeNamed bladeNamed = new ItemSlashBladeNamed(null, 0.0F);
    public static final Map<String, ItemStack> customItemStacks = new LinkedHashMap<>();

    public static void registerCustomItemStack(String bladeId, ItemStack stack) {
        customItemStacks.put(bladeId, stack);
    }

    public static ItemStack findItemStack(String bladeId) {
        return customItemStacks.get(bladeId);
    }
}