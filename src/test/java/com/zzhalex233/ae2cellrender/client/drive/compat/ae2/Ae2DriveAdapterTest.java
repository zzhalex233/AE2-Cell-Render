package com.zzhalex233.ae2cellrender.client.drive.compat.ae2;

import appeng.block.storage.BlockDrive;
import appeng.tile.storage.TileDrive;
import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveSlotVisual;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae2DriveAdapterTest {

    @Test
    void matchesAe2DriveAndReportsTenSlots() {
        Ae2DriveAdapter adapter = new Ae2DriveAdapter(new DriveRenderCache(), positionKey -> {
        });

        assertTrue(adapter.matches(new BlockDrive(), new FakeTileDrive(true, EnumFacing.NORTH, EnumFacing.UP, filledStatuses())));
        assertEquals(10, adapter.slotCount());
    }

    @Test
    void captureClientStateUsesCachedSnapshotAndOrientation() {
        DriveRenderCache cache = new DriveRenderCache();
        FakeTileDrive drive = new FakeTileDrive(true, EnumFacing.SOUTH, EnumFacing.UP, filledStatuses());
        List<byte[]> slots = emptySlots();
        slots.set(0, new byte[]{1, 2, 3});
        cache.store(new DriveRenderSnapshot(2, 99L, slots), Ae2DriveAdapter.buildStateDigest(drive));

        Ae2DriveAdapter adapter = new Ae2DriveAdapter(cache, positionKey -> {
        });
        DriveVisualState state = adapter.captureClientState(null, null, drive, 2, 99L, 123L);

        assertTrue(state.isOnline());
        assertEquals(EnumFacing.SOUTH, state.getForward());
        assertEquals(EnumFacing.UP, state.getUp());
        assertEquals(10, state.getSlots().size());
        assertSlotBytes(state.getSlots().get(0), new byte[]{1, 2, 3});
        assertSlotBytes(state.getSlots().get(9), new byte[0]);
    }

    @Test
    void captureClientStateMarksOfflineDrivesAsNonRendering() {
        FakeTileDrive drive = new FakeTileDrive(false, EnumFacing.NORTH, EnumFacing.UP, filledStatuses());
        Ae2DriveAdapter adapter = new Ae2DriveAdapter(new DriveRenderCache(), positionKey -> {
        });

        DriveVisualState state = adapter.captureClientState(null, null, drive, 0, 42L, 200L);

        assertFalse(state.isOnline());
        assertEquals(10, state.getSlots().size());
    }

    private static void assertSlotBytes(DriveSlotVisual slot, byte[] expected) {
        assertArrayEquals(expected, slot.getSerializedStack());
    }

    private static int[] filledStatuses() {
        int[] statuses = new int[10];
        for (int i = 0; i < statuses.length; i++) {
            statuses[i] = 1;
        }
        return statuses;
    }

    private static List<byte[]> emptySlots() {
        return new ArrayList<>(Collections.nCopies(10, new byte[0]));
    }

    private static final class FakeTileDrive extends TileDrive {
        private final boolean powered;
        private final EnumFacing forward;
        private final EnumFacing up;
        private final int[] statuses;

        private FakeTileDrive(boolean powered, EnumFacing forward, EnumFacing up, int[] statuses) {
            this.powered = powered;
            this.forward = forward;
            this.up = up;
            this.statuses = statuses.clone();
        }

        @Override
        public boolean isPowered() {
            return powered;
        }

        @Override
        public EnumFacing getForward() {
            return forward;
        }

        @Override
        public EnumFacing getUp() {
            return up;
        }

        @Override
        public int getCellStatus(int slot) {
            return statuses[slot];
        }
    }
}
