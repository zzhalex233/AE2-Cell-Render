package com.zzhalex233.ae2cellrender.proxy;

import appeng.tile.storage.TileDrive;
import com.zzhalex233.ae2cellrender.client.drive.CellColorResolver;
import com.zzhalex233.ae2cellrender.client.drive.DriveCellFastRenderer;
import com.zzhalex233.ae2cellrender.client.drive.DriveClientEventHandler;
import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();

        DriveRenderCache cache = DriveRenderCache.getInstance();
        ClientRegistry.bindTileEntitySpecialRenderer(TileDrive.class, new DriveCellFastRenderer(cache));
        new DriveClientEventHandler(cache).register();
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(CellColorResolver.INSTANCE);
    }
}
