package com.zzhalex233.ae2cellrender.drive;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DriveRenderSnapshot {

    private final int dimensionId;
    private final long positionKey;
    private final List<byte[]> slots;

    public DriveRenderSnapshot(int dimensionId, long positionKey, List<byte[]> slots) {
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("Expected at least one slot");
        }
        this.dimensionId = dimensionId;
        this.positionKey = positionKey;
        this.slots = copySlots(slots);
    }

    public static DriveRenderSnapshot empty(int dimensionId, long positionKey, int slotCount) {
        if (slotCount <= 0) {
            throw new IllegalArgumentException("Expected positive slot count");
        }
        List<byte[]> slots = new ArrayList<>(slotCount);
        for (int i = 0; i < slotCount; i++) {
            slots.add(new byte[0]);
        }
        return new DriveRenderSnapshot(dimensionId, positionKey, slots);
    }

    public void writeTo(DataOutput output) throws IOException {
        output.writeInt(dimensionId);
        output.writeLong(positionKey);
        output.writeInt(slots.size());
        for (byte[] slot : slots) {
            output.writeInt(slot.length);
            output.write(slot);
        }
    }

    public static DriveRenderSnapshot readFrom(DataInput input) throws IOException {
        int dimensionId = input.readInt();
        long positionKey = input.readLong();
        int slotCount = input.readInt();
        if (slotCount < 1 || slotCount > 64) {
            throw new IOException("Invalid slot count: " + slotCount);
        }
        List<byte[]> slots = new ArrayList<>(slotCount);
        for (int i = 0; i < slotCount; i++) {
            int length = input.readInt();
            if (length < 0 || length > 8192) {
                throw new IOException("Invalid slot payload size: " + length);
            }
            byte[] payload = new byte[length];
            input.readFully(payload);
            slots.add(payload);
        }
        return new DriveRenderSnapshot(dimensionId, positionKey, slots);
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public long getPositionKey() {
        return positionKey;
    }

    public List<byte[]> getSlots() {
        return slots;
    }

    private static List<byte[]> copySlots(List<byte[]> slots) {
        List<byte[]> copy = new ArrayList<>(slots.size());
        for (byte[] slot : slots) {
            copy.add(slot.clone());
        }
        return Collections.unmodifiableList(copy);
    }
}
