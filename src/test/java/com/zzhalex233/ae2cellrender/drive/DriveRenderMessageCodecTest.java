package com.zzhalex233.ae2cellrender.drive;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DriveRenderMessageCodecTest {

    @Test
    void syncPacketRoundTripsATenSlotSnapshot() throws Exception {
        List<byte[]> slots = new ArrayList<>(Collections.nCopies(10, new byte[0]));
        slots.set(0, new byte[] {1, 2, 3});
        slots.set(9, new byte[] {4, 5});
        DriveRenderSnapshot snapshot = new DriveRenderSnapshot(7, 987654321L, slots);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        snapshot.writeTo(new DataOutputStream(bytes));

        DriveRenderSnapshot decoded = DriveRenderSnapshot.readFrom(
                new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray()))
        );

        assertEquals(7, decoded.getDimensionId());
        assertEquals(987654321L, decoded.getPositionKey());
        assertEquals(10, decoded.getSlots().size());
        assertArrayEquals(new byte[] {1, 2, 3}, decoded.getSlots().get(0));
        assertArrayEquals(new byte[] {4, 5}, decoded.getSlots().get(9));
    }
}
