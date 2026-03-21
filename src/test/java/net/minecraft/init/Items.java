package net.minecraft.init;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class Items {
    public static final Item APPLE = register("minecraft", "apple");
    public static final Item FIERY_STORAGE_CELL_1K = register("aeadditions", "fiery_storage_cell_1k");
    public static final Item FIERY_STORAGE_CELL_16K = register("aeadditions", "fiery_storage_cell_16k");
    public static final Item SOLO_STORAGE_CELL = register("aeadditions", "solo_storage_cell");

    private Items() {
    }

    private static Item register(String domain, String path) {
        Item item = new Item().setRegistryName(new ResourceLocation(domain, path));
        ForgeRegistries.ITEMS.register(item);
        return item;
    }
}
