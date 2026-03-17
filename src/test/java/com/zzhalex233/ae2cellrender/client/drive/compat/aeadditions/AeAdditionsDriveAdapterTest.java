package com.zzhalex233.ae2cellrender.client.drive.compat.aeadditions;

import com.the9grounds.aeadditions.block.BlockHardMEDrive;
import com.the9grounds.aeadditions.tileentity.TileEntityHardMeDrive;
import com.zzhalex233.ae2cellrender.client.drive.DriveRenderCache;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveSlotVisual;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import com.zzhalex233.ae2cellrender.drive.DriveSlotLayouts;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeAdditionsDriveAdapterTest {

    @Test
    void matchesHardMeDriveAndReadsThreeSlots() {
        DriveRenderCache cache = new DriveRenderCache();
        TileEntityHardMeDrive drive = new TileEntityHardMeDrive(true, filledStatuses(3));
        List<byte[]> slots = emptySlots(3);
        slots.set(0, new byte[] {4});
        slots.set(2, new byte[] {7, 8});
        cache.store(new DriveRenderSnapshot(6, 88L, slots), AeAdditionsDriveAdapter.buildStateDigest(drive, 3));

        AeAdditionsDriveAdapter adapter = new AeAdditionsDriveAdapter(cache, positionKey -> {
        });
        DriveVisualState state = adapter.captureClientState(null, null, drive, EnumFacing.NORTH, 6, 88L, 99L);

        assertTrue(adapter.matches(new BlockHardMEDrive(), drive));
        assertEquals(3, adapter.slotCount());
        assertTrue(state.isOnline());
        assertEquals(EnumFacing.NORTH, state.getForward());
        assertEquals(EnumFacing.UP, state.getUp());
        assertEquals(3, state.getSlots().size());
        assertEquals(DriveSlotLayouts.AEADDITIONS_LAYOUT_ID, state.getSlots().get(0).getLayoutId());
        assertSlotBytes(state.getSlots().get(0), new byte[] {4});
        assertSlotBytes(state.getSlots().get(2), new byte[] {7, 8});
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
