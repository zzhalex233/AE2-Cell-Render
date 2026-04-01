package mods.flammpfeil.slashblade.specialattack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class SpecialAttackBase {
    public abstract String toString();

    public abstract void doSpacialAttack(ItemStack itemStack, EntityPlayer entityPlayer);
}