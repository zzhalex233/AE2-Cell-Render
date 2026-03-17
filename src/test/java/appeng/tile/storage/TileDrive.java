package appeng.tile.storage;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileDrive extends TileEntity {

    public boolean isPowered() {
        return false;
    }

    public EnumFacing getForward() {
        return EnumFacing.NORTH;
    }

    public EnumFacing getUp() {
        return EnumFacing.UP;
    }

    public int getCellStatus(int slot) {
        return 0;
    }
}
