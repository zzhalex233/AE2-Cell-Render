package com.zzhalex233.ae2cellrender.mixin.crazyae;

import com.zzhalex233.ae2cellrender.server.drive.DriveRenderHooks;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.beecube31.crazyae2.common.tile.storage.TileImprovedDrive")
public abstract class TileImprovedDriveMixin {

    @Inject(method = "onChangeInventory", at = @At("TAIL"))
    private void ae2cellrender$pushSnapshot(CallbackInfo ci) {
        DriveRenderHooks.pushIfSupported((TileEntity) (Object) this);
    }
}
