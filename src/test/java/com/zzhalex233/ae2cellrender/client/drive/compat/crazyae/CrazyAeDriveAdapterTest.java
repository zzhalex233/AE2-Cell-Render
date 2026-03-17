package com.zzhalex233.ae2cellrender.client.drive.compat.crazyae;

import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveSlotVisual;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import com.zzhalex233.ae2cellrender.drive.DriveSlotLayouts;
import dev.beecube31.crazyae2.common.blocks.storage.BlockDriveImproved;
import dev.beecube31.crazyae2.common.tile.storage.TileImprovedDrive;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrazyAeDriveAdapterTest {

    @Test
    void matchesImprovedDriveAndReadsThirtyFiveSlots() {
        DriveRenderCache cache = new DriveRenderCache();
        TileImprovedDrive drive = new TileImprovedDrive(true, EnumFacing.NORTH, EnumFacing.UP, filledStatuses(35));
        List<byte[]> slots = emptySlots(35);
        slots.set(0, new byte[] {1});
        slots.set(34, new byte[] {9});
        cache.store(new DriveRenderSnapshot(3, 45L, slots), CrazyAeDriveAdapter.buildStateDigest(drive, 35));

        CrazyAeDriveAdapter adapter = new CrazyAeDriveAdapter(cache, positionKey -> {
        });
        DriveVisualState state = adapter.captureClientState(null, null, drive, EnumFacing.NORTH, EnumFacing.UP, 3, 45L, 80L);

        assertTrue(adapter.matches(new BlockDriveImproved(), drive));
        assertEquals(35, adapter.slotCount());
        assertTrue(state.isOnline());
        assertEquals(35, state.getSlots().size());
        assertEquals(DriveSlotLayouts.CRAZYAE_LAYOUT_ID, state.getSlots().get(0).getLayoutId());
        assertSlotBytes(state.getSlots().get(0), new byte[] {1});
        assertSlotBytes(state.getSlots().get(34), new byte[] {9});
    }

    private static int[] filledStatuses(int slotCount) {
        int[] statuses = new int[slotCount];
        for (int i = 0; i < statuses.length; i++) {
            statuses[i] = 1;
        }
        return statuses;
    }

    private static List<byte[]> emptySlots(int slotCount) {
        return new ArrayList<>(Collections.nCopies(slotCount, new byte[0]));
    }

    private static void assertSlotBytes(DriveSlotVisual slot, byte[] expected) {
        assertArrayEquals(expected, slot.getSerializedStack());
    }
}
