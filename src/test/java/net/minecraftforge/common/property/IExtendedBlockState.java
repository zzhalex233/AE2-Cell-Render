package net.minecraftforge.common.property;

import net.minecraft.block.state.IBlockState;

import java.util.Collection;

public interface IExtendedBlockState extends IBlockState {

    Collection<IUnlistedProperty<?>> getUnlistedNames();

    <T> T getValue(IUnlistedProperty<T> property);
}