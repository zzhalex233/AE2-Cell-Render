package com.zzhalex233.ae2cellrender.mixin.ae2;

import appeng.tile.storage.TileDrive;
import appeng.util.inv.InvOperation;
import com.zzhalex233.ae2cellrender.server.drive.DriveRenderHooks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileDrive.class)
public abstract class TileDriveMixin {

    @Inject(method = "onChangeInventory", at = @At("TAIL"))
    private void ae2cellrender$pushSnapshot(
            IItemHandler inventory,
            int slot,
            InvOperation operation,
            ItemStack removed,
            ItemStack added,
            CallbackInfo ci
    ) {
        DriveRenderHooks.onDriveInventoryChanged((TileDrive) (Object) this);
    }
}
