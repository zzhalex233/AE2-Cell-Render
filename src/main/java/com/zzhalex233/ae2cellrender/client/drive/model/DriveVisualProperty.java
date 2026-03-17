package com.zzhalex233.ae2cellrender.client.drive.model;

import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import net.minecraftforge.common.property.IUnlistedProperty;

public final class DriveVisualProperty implements IUnlistedProperty<DriveVisualState> {

    public static final DriveVisualProperty INSTANCE = new DriveVisualProperty();

    private DriveVisualProperty() {
    }

    @Override
    public String getName() {
        return "drive_visual_state";
    }

    @Override
    public boolean isValid(DriveVisualState value) {
        return true;
    }

    @Override
    public Class<DriveVisualState> getType() {
        return DriveVisualState.class;
    }

    @Override
    public String valueToString(DriveVisualState value) {
        if (value == null) {
            return "null";
        }
        return "online=" + value.isOnline() + ",slots=" + value.getSlots().size();
    }
}
