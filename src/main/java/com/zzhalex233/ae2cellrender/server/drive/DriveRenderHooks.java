package com.zzhalex233.ae2cellrender.server.drive;

import appeng.tile.storage.TileDrive;
import com.zzhalex233.ae2cellrender.client.drive.compat.ae2.Ae2DriveAdapter;
import com.zzhalex233.ae2cellrender.client.drive.compat.aeadditions.AeAdditionsDriveAdapter;
import com.zzhalex233.ae2cellrender.client.drive.compat.crazyae.CrazyAeDriveAdapter;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import com.zzhalex233.ae2cellrender.network.AE2CellRenderNetwork;
import com.zzhalex233.ae2cellrender.network.message.SyncDriveRenderDataMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public final class DriveRenderHooks {

    private static final String CRAZYAE_TILE = "dev.beecube31.crazyae2.common.tile.storage.TileImprovedDrive";
    private static final String AEADDITIONS_TILE = "com.the9grounds.aeadditions.tileentity.TileEntityHardMeDrive";
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
        if (matchesClass(tile, CRAZYAE_TILE)) {
            IItemHandler inventory = invokeItemHandler(tile, "getInternalInventory");
            if (inventory == null) {
                return false;
            }
            int slotCount = invokeInt(tile, "getCellCount", inventory.getSlots());
            pushSnapshot(tile, capture(tile.getWorld().provider.getDimension(), tile.getPos().toLong(), inventory), CrazyAeDriveAdapter.buildStateDigest(tile, slotCount));
            return true;
        }
        if (matchesClass(tile, AEADDITIONS_TILE)) {
            IInventory inventory = invokeInventory(tile, "getInventory");
            if (inventory == null) {
                return false;
            }
            int slotCount = invokeInt(tile, "getCellCount", inventory.getSizeInventory());
            pushSnapshot(tile, capture(tile.getWorld().provider.getDimension(), tile.getPos().toLong(), inventory), AeAdditionsDriveAdapter.buildStateDigest(tile, slotCount));
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
        if (matchesClass(tile, CRAZYAE_TILE)) {
            IItemHandler inventory = invokeItemHandler(tile, "getInternalInventory");
            if (inventory == null) {
                return false;
            }
            int slotCount = invokeInt(tile, "getCellCount", inventory.getSlots());
            AE2CellRenderNetwork.CHANNEL.sendTo(
                    new SyncDriveRenderDataMessage(capture(tile.getWorld().provider.getDimension(), tile.getPos().toLong(), inventory), CrazyAeDriveAdapter.buildStateDigest(tile, slotCount)),
                    player
            );
            return true;
        }
        if (matchesClass(tile, AEADDITIONS_TILE)) {
            IInventory inventory = invokeInventory(tile, "getInventory");
            if (inventory == null) {
                return false;
            }
            int slotCount = invokeInt(tile, "getCellCount", inventory.getSizeInventory());
            AE2CellRenderNetwork.CHANNEL.sendTo(
                    new SyncDriveRenderDataMessage(capture(tile.getWorld().provider.getDimension(), tile.getPos().toLong(), inventory), AeAdditionsDriveAdapter.buildStateDigest(tile, slotCount)),
                    player
            );
            return true;
        }
        return false;
    }

    public static DriveRenderSnapshot capture(TileDrive drive) {
        return capture(
                drive.getWorld().provider.getDimension(),
                drive.getPos().toLong(),
                drive.getInternalInventory()
        );
    }

    public static DriveRenderSnapshot capture(int dimensionId, long positionKey, IItemHandler inventory) {
        List<byte[]> slots = new ArrayList<>(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            slots.add(serializeSlot(inventory.getStackInSlot(i)));
        }
        return new DriveRenderSnapshot(dimensionId, positionKey, slots);
    }

    public static DriveRenderSnapshot capture(int dimensionId, long positionKey, IInventory inventory) {
        List<byte[]> slots = new ArrayList<>(inventory.getSizeInventory());
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
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
        } finally {
            buffer.release();
        }
    }

    private static boolean matchesClass(TileEntity tile, String className) {
        return tile.getClass().getName().equals(className);
    }

    private static boolean hasWorldContext(TileEntity tile) {
        return tile != null && tile.getWorld() != null;
    }

    private static int invokeInt(Object target, String methodName, int fallback) {
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            return value instanceof Number ? ((Number) value).intValue() : fallback;
        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }

    private static IItemHandler invokeItemHandler(Object target, String methodName) {
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            return value instanceof IItemHandler ? (IItemHandler) value : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static IInventory invokeInventory(Object target, String methodName) {
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            return value instanceof IInventory ? (IInventory) value : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
