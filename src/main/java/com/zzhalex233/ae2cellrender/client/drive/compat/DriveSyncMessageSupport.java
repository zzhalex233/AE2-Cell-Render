package com.zzhalex233.ae2cellrender.client.drive.compat;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public final class DriveSyncMessageSupport {

    private DriveSyncMessageSupport() {
    }

    public static boolean accepts(@Nullable Block block, @Nullable TileEntity tile) {
        return DriveAdapterRegistry.findAdapter(block, tile) != null;
    }
}
