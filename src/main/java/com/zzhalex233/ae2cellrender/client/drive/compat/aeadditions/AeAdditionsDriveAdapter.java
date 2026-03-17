package com.zzhalex233.ae2cellrender.client.drive.compat.aeadditions;

import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveAdapter;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveSlotVisual;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import com.zzhalex233.ae2cellrender.client.drive.compat.ReflectiveDriveAccess;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import com.zzhalex233.ae2cellrender.drive.DriveSlotLayouts;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class AeAdditionsDriveAdapter implements DriveAdapter {

    private static final String BLOCK_CLASS = "com.the9grounds.aeadditions.block.BlockHardMEDrive";
    private static final String TILE_CLASS = "com.the9grounds.aeadditions.tileentity.TileEntityHardMeDrive";
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation("aeadditions", "builtin/hard_drive");

    private final DriveRenderCache cache;
    private final SnapshotRequester snapshotRequester;

    public AeAdditionsDriveAdapter(DriveRenderCache cache) {
        this(cache, new NetworkSnapshotRequester());
    }

    AeAdditionsDriveAdapter(DriveRenderCache cache, SnapshotRequester snapshotRequester) {
        this.cache = cache;
        this.snapshotRequester = snapshotRequester;
    }

    @Override
    public boolean matches(Block block, @Nullable TileEntity tile) {
        return ReflectiveDriveAccess.matchesClass(block, BLOCK_CLASS) || ReflectiveDriveAccess.matchesClass(tile, TILE_CLASS);
    }

    @Override
    public int slotCount() {
        return DriveSlotLayouts.forId(DriveSlotLayouts.AEADDITIONS_LAYOUT_ID).slotCount();
    }

    @Override
    public DriveVisualState captureClientState(World world, BlockPos pos, TileEntity tile) {
        if (!ReflectiveDriveAccess.matchesClass(tile, TILE_CLASS)) {
            return emptyState();
        }

        int slotCount = ReflectiveDriveAccess.invokeInt(tile, "getCellCount", slotCount());
        EnumFacing forward = resolveFacing(world, pos);
        return captureClientState(world, pos, tile, forward, world.provider.getDimension(), pos.toLong(), world.getTotalWorldTime(), slotCount);
    }

    DriveVisualState captureClientState(@Nullable World world, @Nullable BlockPos pos, Object drive, EnumFacing forward, int dimensionId, long positionKey, long worldTime) {
        int slotCount = ReflectiveDriveAccess.invokeInt(drive, "getCellCount", slotCount());
        return captureClientState(world, pos, drive, forward, dimensionId, positionKey, worldTime, slotCount);
    }

    private DriveVisualState captureClientState(@Nullable World world, @Nullable BlockPos pos, Object drive, EnumFacing forward, int dimensionId, long positionKey, long worldTime, int slotCount) {
        int digest = buildStateDigest(drive, slotCount);
        DriveRenderSnapshot snapshot = cache.getSnapshot(dimensionId, positionKey, digest);
        if (snapshot == null && world != null && world.isRemote && pos != null && cache.shouldRequest(dimensionId, positionKey, digest, worldTime)) {
            snapshotRequester.request(positionKey);
        }

        return new DriveVisualState(
                ReflectiveDriveAccess.invokeBoolean(drive, "isPowered"),
                forward,
                EnumFacing.UP,
                buildSlotVisuals(snapshot, slotCount)
        );
    }

    @Override
    public ResourceLocation modelLocation() {
        return MODEL_LOCATION;
    }

    public static int buildStateDigest(Object drive, int slotCount) {
        int digest = 1;
        for (int slot = 0; slot < slotCount; slot++) {
            digest = 31 * digest + ReflectiveDriveAccess.invokeInt(drive, "getCellStatus", 0, slot);
        }
        return digest;
    }

    private EnumFacing resolveFacing(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        for (IProperty<?> property : state.getPropertyKeys()) {
            if ("facing".equals(property.getName())) {
                Comparable<?> value = state.getValue(property);
                if (value instanceof EnumFacing) {
                    return (EnumFacing) value;
                }
            }
        }
        return EnumFacing.NORTH;
    }

    private DriveVisualState emptyState() {
        return new DriveVisualState(false, EnumFacing.NORTH, EnumFacing.UP, buildSlotVisuals(null, slotCount()));
    }

    private List<DriveSlotVisual> buildSlotVisuals(@Nullable DriveRenderSnapshot snapshot, int slotCount) {
        List<DriveSlotVisual> slots = new ArrayList<>(slotCount);
        for (int slot = 0; slot < slotCount; slot++) {
            byte[] payload = snapshot == null ? new byte[0] : snapshot.getSlots().get(slot);
            slots.add(new DriveSlotVisual(slot, payload, DriveSlotLayouts.AEADDITIONS_LAYOUT_ID));
        }
        return slots;
    }

    interface SnapshotRequester {
        void request(long positionKey);
    }

    private static final class NetworkSnapshotRequester implements SnapshotRequester {

        @Override
        public void request(long positionKey) {
            com.zzhalex233.ae2cellrender.network.AE2CellRenderNetwork.CHANNEL.sendToServer(
                    new com.zzhalex233.ae2cellrender.network.message.RequestDriveRenderDataMessage(positionKey)
            );
        }
    }
}
