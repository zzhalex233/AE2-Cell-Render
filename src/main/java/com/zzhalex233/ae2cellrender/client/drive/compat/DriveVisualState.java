package com.zzhalex233.ae2cellrender.client.drive.compat;

import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DriveVisualState {

    private final boolean online;
    private final EnumFacing forward;
    private final EnumFacing up;
    private final List<DriveSlotVisual> slots;

    public DriveVisualState(boolean online, EnumFacing forward, EnumFacing up, List<DriveSlotVisual> slots) {
        this.online = online;
        this.forward = forward;
        this.up = up;
        this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
    }

    public boolean isOnline() {
        return online;
    }

    public EnumFacing getForward() {
        return forward;
    }

    public EnumFacing getUp() {
        return up;
    }

    public List<DriveSlotVisual> getSlots() {
        return slots;
    }
}
