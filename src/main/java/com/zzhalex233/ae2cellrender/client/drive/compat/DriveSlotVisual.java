package com.zzhalex233.ae2cellrender.client.drive.compat;

import javax.annotation.Nullable;
import java.util.Arrays;

public final class DriveSlotVisual {

    private final int slotIndex;
    private final byte[] serializedStack;
    @Nullable
    private final String layoutId;

    public DriveSlotVisual(int slotIndex, byte[] serializedStack, @Nullable String layoutId) {
        this.slotIndex = slotIndex;
        this.serializedStack = serializedStack.clone();
        this.layoutId = layoutId;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public byte[] getSerializedStack() {
        return serializedStack.clone();
    }

    @Nullable
    public String getLayoutId() {
        return layoutId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DriveSlotVisual)) {
            return false;
        }
        DriveSlotVisual that = (DriveSlotVisual) other;
        return slotIndex == that.slotIndex
                && Arrays.equals(serializedStack, that.serializedStack)
                && java.util.Objects.equals(layoutId, that.layoutId);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(slotIndex);
        result = 31 * result + Arrays.hashCode(serializedStack);
        result = 31 * result + java.util.Objects.hashCode(layoutId);
        return result;
    }
}
