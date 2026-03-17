package com.zzhalex233.ae2cellrender.client.drive;

import com.zzhalex233.ae2cellrender.client.drive.compat.DriveSlotVisual;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveVisualStateTest {

    @Test
    void exposesOnlineFlagAndSlotLayout() {
        DriveSlotVisual slot = new DriveSlotVisual(0, new byte[] {1}, "default");
        DriveVisualState state = new DriveVisualState(true, null, null, Arrays.asList(slot));

        assertTrue(state.isOnline());
        assertEquals(1, state.getSlots().size());
        assertEquals(slot, state.getSlots().get(0));
        assertEquals("default", slot.getLayoutId());
    }

    @Test
    void slotListIsImmutableAndBytesAreDefensivelyCopied() {
        DriveSlotVisual slot = new DriveSlotVisual(0, new byte[] {5}, null);
        DriveVisualState state = new DriveVisualState(false, null, null, Collections.singletonList(slot));

        assertFalse(state.isOnline());
        assertThrows(UnsupportedOperationException.class, () -> state.getSlots().add(slot));

        byte[] exported = slot.getSerializedStack();
        exported[0] = 42;
        assertArrayEquals(new byte[] {5}, slot.getSerializedStack());
    }

    @Test
    void copiesIncomingSlotList() {
        DriveSlotVisual slot = new DriveSlotVisual(0, new byte[] {1, 2, 3}, "layout");
        List<DriveSlotVisual> source = new java.util.ArrayList<>(Arrays.asList(slot));
        DriveVisualState state = new DriveVisualState(false, null, null, source);

        source.set(0, new DriveSlotVisual(1, new byte[] {9}, "other"));
        assertEquals(0, state.getSlots().get(0).getSlotIndex());
        assertArrayEquals(new byte[] {1, 2, 3}, state.getSlots().get(0).getSerializedStack());
    }
}
