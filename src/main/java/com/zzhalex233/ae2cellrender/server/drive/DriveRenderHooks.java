package com.zzhalex233.ae2cellrender.server.drive;

import appeng.tile.storage.TileDrive;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import com.zzhalex233.ae2cellrender.network.AE2CellRenderNetwork;
import com.zzhalex233.ae2cellrender.network.message.SyncDriveRenderDataMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public final class DriveRenderHooks {

    private static final double PUSH_RADIUS = 128.0D;

    private DriveRenderHooks() {
    }

    public static void onDriveInventoryChanged(TileDrive drive) {
        if (drive.getWorld() == null || drive.getWorld().isRemote) {
            return;
        }

        BlockPos pos = drive.getPos();
        AE2CellRenderNetwork.CHANNEL.sendToAllAround(
                new SyncDriveRenderDataMessage(capture(drive)),
                new NetworkRegistry.TargetPoint(
                        drive.getWorld().provider.getDimension(),
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        PUSH_RADIUS
                )
        );
    }

    public static void sendToPlayer(TileDrive drive, EntityPlayerMP player) {
        AE2CellRenderNetwork.CHANNEL.sendTo(new SyncDriveRenderDataMessage(capture(drive)), player);
    }

    public static DriveRenderSnapshot capture(TileDrive drive) {
        IItemHandler inventory = drive.getInternalInventory();
        List<byte[]> slots = new ArrayList<>(DriveRenderSnapshot.SLOT_COUNT);
        for (int i = 0; i < DriveRenderSnapshot.SLOT_COUNT; i++) {
            slots.add(serializeSlot(inventory.getStackInSlot(i)));
        }
        return new DriveRenderSnapshot(drive.getWorld().provider.getDimension(), drive.getPos().toLong(), slots);
    }

    private static byte[] serializeSlot(ItemStack stack) {
        ItemStack normalized = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (!normalized.isEmpty()) {
            normalized.setCount(1);
        }
        ByteBuf buffer = Unpooled.buffer();
        try {
            ByteBufUtils.writeItemStack(buffer, normalized);
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally {
            buffer.release();
        }
    }
}
