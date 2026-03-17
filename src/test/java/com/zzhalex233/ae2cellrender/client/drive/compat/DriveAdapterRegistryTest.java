package com.zzhalex233.ae2cellrender.client.drive.compat;

import appeng.block.storage.BlockDrive;
import appeng.tile.storage.TileDrive;
import com.the9grounds.aeadditions.block.BlockHardMEDrive;
import com.the9grounds.aeadditions.tileentity.TileEntityHardMeDrive;
import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import com.zzhalex233.ae2cellrender.client.drive.compat.ae2.Ae2DriveAdapter;
import com.zzhalex233.ae2cellrender.client.drive.compat.aeadditions.AeAdditionsDriveAdapter;
import com.zzhalex233.ae2cellrender.client.drive.compat.crazyae.CrazyAeDriveAdapter;
import dev.beecube31.crazyae2.common.blocks.storage.BlockDriveImproved;
import dev.beecube31.crazyae2.common.tile.storage.TileImprovedDrive;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DriveAdapterRegistryTest {

    @BeforeEach
    void clearRegistry() {
        DriveAdapterRegistry.reset();
    }

    @Test
    void returnsFirstMatchingAdapter() {
        DriveAdapter first = new TestAdapter(true);
        DriveAdapter second = new TestAdapter(true);

        DriveAdapterRegistry.register(first);
        DriveAdapterRegistry.register(second);

        assertSame(first, DriveAdapterRegistry.findAdapter(new Block(), null));
    }

    @Test
    void returnsNullWhenUnsupported() {
        DriveAdapterRegistry.register(new TestAdapter(false));

        assertNull(DriveAdapterRegistry.findAdapter(null, null));
    }

    @Test
    void identifiesAllRegisteredDriveTilesForSyncHandling() {
        DriveRenderCache cache = new DriveRenderCache();
        DriveAdapterRegistry.register(new Ae2DriveAdapter(cache));
        DriveAdapterRegistry.register(new CrazyAeDriveAdapter(cache));
        DriveAdapterRegistry.register(new AeAdditionsDriveAdapter(cache));

        assertTrue(DriveSyncMessageSupport.accepts(new BlockDrive(), new TileDrive()));
        assertTrue(DriveSyncMessageSupport.accepts(
                new BlockDriveImproved(),
                new TileImprovedDrive(true, EnumFacing.NORTH, EnumFacing.UP, new int[35])
        ));
        assertTrue(DriveSyncMessageSupport.accepts(
                new BlockHardMEDrive(),
                new TileEntityHardMeDrive(true, new int[3])
        ));
        assertFalse(DriveSyncMessageSupport.accepts(new Block(), new TileEntity()));
    }

    private static final class TestAdapter implements DriveAdapter {

        private final boolean shouldMatch;

        private TestAdapter(boolean shouldMatch) {
            this.shouldMatch = shouldMatch;
        }

        @Override
        public boolean matches(Block block, TileEntity tile) {
            return shouldMatch;
        }

        @Override
        public int slotCount() {
            return 1;
        }

        @Override
        public DriveVisualState captureClientState(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, TileEntity tile) {
            return new DriveVisualState(true, EnumFacing.NORTH, EnumFacing.UP, Collections.emptyList());
        }

        @Override
        public net.minecraft.util.ResourceLocation modelLocation() {
            return new net.minecraft.util.ResourceLocation("drive", "test");
        }
    }
}
