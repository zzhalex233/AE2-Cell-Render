package net.minecraft.client;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.color.ItemColors;

public class Minecraft {
    private static final Minecraft INSTANCE = new Minecraft();

    private boolean callingFromMinecraftThread = true;
    private final RenderItem renderItem = new RenderItem();
    private final ItemColors itemColors = new ItemColors();
    public WorldClient world;

    public static Minecraft getMinecraft() {
        return INSTANCE;
    }

    public boolean isCallingFromMinecraftThread() {
        return callingFromMinecraftThread;
    }

    public void setCallingFromMinecraftThread(boolean callingFromMinecraftThread) {
        this.callingFromMinecraftThread = callingFromMinecraftThread;
    }

    public RenderItem getRenderItem() {
        return renderItem;
    }

    public ItemColors getItemColors() {
        return itemColors;
    }

    public void reset() {
        callingFromMinecraftThread = true;
        renderItem.clearModels();
        renderItem.setItemColors(itemColors);
        itemColors.setColorMultiplier((stack, tintIndex) -> -1);
        world = null;
    }
}