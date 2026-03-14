package com.zzhalex233.ae2cellrender.client.drive;

import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DriveRenderCacheTest {

    @Test
    void throttlesRepeatedRequestsForTheSameDriveAndDigest() {
        DriveRenderCache cache = new DriveRenderCache();
        long positionKey = 123L;

        assertTrue(cache.shouldRequest(0, positionKey, 0x15, 100L));
        assertFalse(cache.shouldRequest(0, positionKey, 0x15, 105L));
        assertTrue(cache.shouldRequest(0, positionKey, 0x15, 121L));
    }

    @Test
    void stateDigestChangeBypassesTheCooldown() {
        DriveRenderCache cache = new DriveRenderCache();
        long positionKey = 123L;

        assertTrue(cache.shouldRequest(0, positionKey, 0x15, 100L));
        assertTrue(cache.shouldRequest(0, positionKey, 0x19, 101L));
    }

    @Test
    void returnsSnapshotsOnlyWhenTheirDigestMatchesTheCurrentState() {
        DriveRenderCache cache = new DriveRenderCache();
        List<byte[]> slots = new ArrayList<>(Collections.nCopies(10, new byte[0]));
        slots.set(0, new byte[] {1, 2, 3});
        DriveRenderSnapshot snapshot = new DriveRenderSnapshot(3, 456L, slots);

        cache.store(snapshot, 0x15);

        DriveRenderSnapshot current = cache.getSnapshot(3, 456L, 0x15);
        assertNotNull(current);
        assertArrayEquals(new byte[] {1, 2, 3}, current.getSlots().get(0));
        assertNull(cache.getSnapshot(3, 456L, 0x19));
    }
}
