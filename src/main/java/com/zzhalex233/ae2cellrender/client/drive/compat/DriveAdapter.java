package com.zzhalex233.ae2cellrender.client.drive.compat;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface DriveAdapter {

    boolean matches(Block block, @Nullable TileEntity tile);

    int slotCount();

    DriveVisualState captureClientState(World world, BlockPos pos, TileEntity tile);

    ResourceLocation modelLocation();
}
