package com.zzhalex233.ae2cellrender.mixin.ae2;

import appeng.block.storage.BlockDrive;
import com.zzhalex233.ae2cellrender.client.drive.model.DriveVisualStateHooks;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockDrive.class)
public abstract class BlockDriveMixin {

    @Inject(method = "createBlockState", at = @At("RETURN"), cancellable = true)
    private void ae2cellrender$addVisualProperty(CallbackInfoReturnable<BlockStateContainer> cir) {
        cir.setReturnValue(DriveVisualStateHooks.appendVisualProperty((Block) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "getExtendedState", at = @At("RETURN"), cancellable = true)
    private void ae2cellrender$attachVisualState(IBlockState state, IBlockAccess world, BlockPos pos, CallbackInfoReturnable<IBlockState> cir) {
        cir.setReturnValue(DriveVisualStateHooks.attachVisualState((Block) (Object) this, cir.getReturnValue(), world, pos));
    }
}
