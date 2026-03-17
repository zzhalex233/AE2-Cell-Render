package com.zzhalex233.ae2cellrender.client.drive.compat.ae2;

import appeng.block.storage.BlockDrive;
import appeng.tile.storage.TileDrive;
import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveAdapter;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveSlotVisual;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import com.zzhalex233.ae2cellrender.drive.DriveCellSlotLayout;
import com.zzhalex233.ae2cellrender.drive.DriveSlotLayouts;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class Ae2DriveAdapter implements DriveAdapter {

    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation("appliedenergistics2", "drive");

    private final DriveRenderCache cache;
    private final SnapshotRequester snapshotRequester;

    public Ae2DriveAdapter(DriveRenderCache cache) {
        this(cache, new NetworkSnapshotRequester());
    }

    Ae2DriveAdapter(DriveRenderCache cache, SnapshotRequester snapshotRequester) {
        this.cache = cache;
        this.snapshotRequester = snapshotRequester;
    }

    @Override
    public boolean matches(Block block, @Nullable TileEntity tile) {
        return block instanceof BlockDrive || tile instanceof TileDrive;
    }

    @Override
    public int slotCount() {
        return DriveCellSlotLayout.SLOT_COUNT;
    }

    @Override
    public DriveVisualState captureClientState(World world, BlockPos pos, TileEntity tile) {
        TileDrive drive = tile instanceof TileDrive ? (TileDrive) tile : null;
        if (drive == null) {
            return emptyState();
        }

        int dimensionId = world.provider.getDimension();
        long positionKey = pos.toLong();
        long worldTime = world.getTotalWorldTime();
        return captureClientState(world, pos, drive, dimensionId, positionKey, worldTime);
    }

    DriveVisualState captureClientState(@Nullable World world, @Nullable BlockPos pos, TileDrive drive, int dimensionId, long positionKey, long worldTime) {
        int digest = buildStateDigest(drive);
        DriveRenderSnapshot snapshot = cache.getSnapshot(dimensionId, positionKey, digest);
        if (snapshot == null && world != null && world.isRemote && pos != null && cache.shouldRequest(dimensionId, positionKey, digest, worldTime)) {
            snapshotRequester.request(positionKey);
        }

        return new DriveVisualState(
                drive.isPowered(),
                drive.getForward(),
                drive.getUp(),
                buildSlotVisuals(snapshot)
        );
    }

    @Override
    public ResourceLocation modelLocation() {
        return MODEL_LOCATION;
    }

    public static int buildStateDigest(TileDrive drive) {
        int digest = 1;
        for (int slot = 0; slot < DriveCellSlotLayout.SLOT_COUNT; slot++) {
            digest = 31 * digest + drive.getCellStatus(slot);
        }
        return digest;
    }

    private DriveVisualState emptyState() {
        return new DriveVisualState(false, EnumFacing.NORTH, EnumFacing.UP, buildSlotVisuals(null));
    }

    private List<DriveSlotVisual> buildSlotVisuals(@Nullable DriveRenderSnapshot snapshot) {
        List<DriveSlotVisual> slots = new ArrayList<>(DriveCellSlotLayout.SLOT_COUNT);
        for (int slot = 0; slot < DriveCellSlotLayout.SLOT_COUNT; slot++) {
            byte[] payload = snapshot == null ? new byte[0] : snapshot.getSlots().get(slot);
            slots.add(new DriveSlotVisual(slot, payload, DriveSlotLayouts.AE2_LAYOUT_ID));
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
