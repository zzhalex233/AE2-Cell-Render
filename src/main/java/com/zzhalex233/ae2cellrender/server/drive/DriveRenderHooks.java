package com.zzhalex233.ae2cellrender.server.drive;

import appeng.tile.storage.TileDrive;
import com.zzhalex233.ae2cellrender.AE2CellRender;
import com.zzhalex233.ae2cellrender.client.drive.compat.ae2.Ae2DriveAdapter;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import com.zzhalex233.ae2cellrender.network.AE2CellRenderNetwork;
import com.zzhalex233.ae2cellrender.network.message.SyncDriveRenderDataMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
        if (!hasWorldContext(drive)) {
            return;
        }
        pushSnapshot(drive, capture(drive), Ae2DriveAdapter.buildStateDigest(drive));
    }

    public static void sendToPlayer(TileDrive drive, EntityPlayerMP player) {
        if (!hasWorldContext(drive)) {
            return;
        }
        AE2CellRenderNetwork.CHANNEL.sendTo(
                new SyncDriveRenderDataMessage(capture(drive), Ae2DriveAdapter.buildStateDigest(drive)),
                player
        );
    }

    public static boolean pushIfSupported(TileEntity tile) {
        if (!hasWorldContext(tile)) {
            return false;
        }
        if (tile instanceof TileDrive) {
            onDriveInventoryChanged((TileDrive) tile);
            return true;
        }
        return false;
    }

    public static boolean sendToPlayerIfSupported(TileEntity tile, EntityPlayerMP player) {
        if (!hasWorldContext(tile)) {
            return false;
        }
        if (tile instanceof TileDrive) {
            sendToPlayer((TileDrive) tile, player);
            return true;
        }
        return false;
    }

    private static DriveRenderSnapshot capture(TileDrive drive) {
        return capture(
                drive.getWorld().provider.getDimension(),
                drive.getPos().toLong(),
                drive.getInternalInventory()
        );
    }

    private static DriveRenderSnapshot capture(int dimensionId, long positionKey, IItemHandler inventory) {
        List<byte[]> slots = new ArrayList<>(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            slots.add(serializeSlot(inventory.getStackInSlot(i)));
        }
        return new DriveRenderSnapshot(dimensionId, positionKey, slots);
    }

    private static void pushSnapshot(TileEntity tile, DriveRenderSnapshot snapshot, int digest) {
        if (tile.getWorld() == null || tile.getWorld().isRemote) {
            return;
        }

        BlockPos pos = tile.getPos();
        AE2CellRenderNetwork.CHANNEL.sendToAllAround(
                new SyncDriveRenderDataMessage(snapshot, digest),
                new NetworkRegistry.TargetPoint(
                        tile.getWorld().provider.getDimension(),
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        PUSH_RADIUS
                )
        );
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
        } catch (Exception e) {
            AE2CellRender.LOGGER.warn("Failed to serialize drive slot", e);
            return new byte[0];
        } finally {
            buffer.release();
        }
    }

    private static boolean hasWorldContext(TileEntity tile) {
        return tile != null && tile.getWorld() != null;
    }
}
