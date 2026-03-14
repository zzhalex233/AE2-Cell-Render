package com.zzhalex233.ae2cellrender.network;

import com.zzhalex233.ae2cellrender.AE2CellRender;
import com.zzhalex233.ae2cellrender.network.message.RequestDriveRenderDataMessage;
import com.zzhalex233.ae2cellrender.network.message.SyncDriveRenderDataMessage;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class AE2CellRenderNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(AE2CellRender.MOD_ID);

    private AE2CellRenderNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(RequestDriveRenderDataMessage.Handler.class, RequestDriveRenderDataMessage.class, 0, Side.SERVER);
        CHANNEL.registerMessage(SyncDriveRenderDataMessage.Handler.class, SyncDriveRenderDataMessage.class, 1, Side.CLIENT);
    }
}
