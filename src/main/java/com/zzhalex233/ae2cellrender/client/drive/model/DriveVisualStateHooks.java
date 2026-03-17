package com.zzhalex233.ae2cellrender.client.drive.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveAdapter;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveAdapterRegistry;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DriveVisualStateHooks {

    private DriveVisualStateHooks() {
    }

    public static BlockStateContainer appendVisualProperty(Block block, BlockStateContainer original) {
        if (!(original instanceof ExtendedBlockState)) {
            return original;
        }

        ExtendedBlockState extended = (ExtendedBlockState) original;
        IProperty<?>[] listed = original.getProperties().toArray(new IProperty[0]);
        IUnlistedProperty<?>[] unlisted = appendVisualProperty(extended.getUnlistedProperties());
        return new ExtendedBlockState(block, listed, unlisted);
    }

    public static IBlockState attachVisualState(Block block, IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState)) {
            return state;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile == null || tile.getWorld() == null || !tile.getWorld().isRemote) {
            return state;
        }

        DriveAdapter adapter = DriveAdapterRegistry.findAdapter(block, tile);
        if (adapter == null) {
            return state;
        }

        DriveVisualState visualState = adapter.captureClientState(tile.getWorld(), pos, tile);
        return ((IExtendedBlockState) state).withProperty(DriveVisualProperty.INSTANCE, visualState);
    }

    private static IUnlistedProperty<?>[] appendVisualProperty(Collection<IUnlistedProperty<?>> existing) {
        List<IUnlistedProperty<?>> merged = new ArrayList<>(existing);
        if (!ImmutableSet.copyOf(existing).contains(DriveVisualProperty.INSTANCE)) {
            merged.add(DriveVisualProperty.INSTANCE);
        }
        return ImmutableList.copyOf(merged).toArray(new IUnlistedProperty[0]);
    }
}
