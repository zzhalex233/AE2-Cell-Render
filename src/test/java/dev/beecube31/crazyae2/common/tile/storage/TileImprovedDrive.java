package dev.beecube31.crazyae2.common.tile.storage;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileImprovedDrive extends TileEntity {

    private final boolean powered;
    private final EnumFacing forward;
    private final EnumFacing up;
    private final int[] statuses;

    public TileImprovedDrive(boolean powered, EnumFacing forward, EnumFacing up, int[] statuses) {
        this.powered = powered;
        this.forward = forward;
        this.up = up;
        this.statuses = statuses.clone();
    }

    public boolean isPowered() {
        return powered;
    }

    public EnumFacing getForward() {
        return forward;
    }

    public EnumFacing getUp() {
        return up;
    }

    public int getCellStatus(int slot) {
        return statuses[slot];
    }

    public int getCellCount() {
        return statuses.length;
    }
}
