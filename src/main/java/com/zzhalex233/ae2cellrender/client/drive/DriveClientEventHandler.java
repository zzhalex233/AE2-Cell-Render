package com.zzhalex233.ae2cellrender.client.drive;

import com.zzhalex233.ae2cellrender.AE2CellRender;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveAdapter;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveAdapterRegistry;
import com.zzhalex233.ae2cellrender.client.drive.model.DriveModelBakeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class DriveClientEventHandler {

    private static final ResourceLocation OVERLAY_SPRITE = new ResourceLocation(AE2CellRender.MOD_ID, "blocks/drive_overlay_white");

    private final DriveRenderCache cache;

    public DriveClientEventHandler(DriveRenderCache cache) {
        this.cache = cache;
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        for (DriveAdapter adapter : DriveAdapterRegistry.getRegisteredAdapters()) {
            DriveModelBakeRegistry.register(new ModelResourceLocation(adapter.modelLocation(), "normal"));
        }
    }

    public static TextureAtlasSprite getOverlaySprite() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(OVERLAY_SPRITE.toString());
    }

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(OVERLAY_SPRITE);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        for (ModelResourceLocation target : DriveModelBakeRegistry.getTargets()) {
            IBakedModel model = event.getModelRegistry().getObject(target);
            if (model != null) {
                event.getModelRegistry().putObject(target, DriveModelBakeRegistry.wrap(target, model));
            }
        }
    }

    @SubscribeEvent
    public void onClientWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            return;
        }

        cache.clear();
        CellColorResolver.INSTANCE.clear();
    }

    @SubscribeEvent
    public void onClientChunkUnload(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            return;
        }

        cache.clear();
    }
}
