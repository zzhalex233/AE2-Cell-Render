package com.zzhalex233.ae2cellrender.proxy;

import com.zzhalex233.ae2cellrender.client.drive.DriveClientEventHandler;
import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveAdapterRegistry;
import com.zzhalex233.ae2cellrender.client.drive.compat.ae2.Ae2DriveAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();

        DriveRenderCache cache = DriveRenderCache.getInstance();
        DriveAdapterRegistry.reset();
        DriveAdapterRegistry.register(new Ae2DriveAdapter(cache));
        DriveClientEventHandler handler = new DriveClientEventHandler(cache);
        handler.register();
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(handler);
    }
}
