package com.zzhalex233.ae2cellrender.drive;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DriveRenderSnapshot {

    public static final int SLOT_COUNT = 10;

    private final int dimensionId;
    private final long positionKey;
    private final List<byte[]> slots;

    public DriveRenderSnapshot(int dimensionId, long positionKey, List<byte[]> slots) {
        if (slots.size() != SLOT_COUNT) {
            throw new IllegalArgumentException("Expected " + SLOT_COUNT + " slots, got " + slots.size());
        }
        this.dimensionId = dimensionId;
        this.positionKey = positionKey;
        this.slots = copySlots(slots);
    }

    public static DriveRenderSnapshot empty(int dimensionId, long positionKey) {
        List<byte[]> slots = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
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
        List<byte[]> slots = new ArrayList<>(slotCount);
        for (int i = 0; i < slotCount; i++) {
            int length = input.readInt();
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
