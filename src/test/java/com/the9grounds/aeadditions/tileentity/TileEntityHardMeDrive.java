package com.the9grounds.aeadditions.tileentity;

import net.minecraft.tileentity.TileEntity;

public class TileEntityHardMeDrive extends TileEntity {

    private final boolean powered;
    private final int[] statuses;

    public TileEntityHardMeDrive(boolean powered, int[] statuses) {
        this.powered = powered;
        this.statuses = statuses.clone();
    }

    public boolean isPowered() {
        return powered;
    }

    public int getCellStatus(int slot) {
        return statuses[slot];
    }

    public int getCellCount() {
        return statuses.length;
    }
}
