package com.zzhalex.slashbladetweaker.bridge;

import com.zzhalex.slashbladetweaker.api.zenscript.support.SpecialEffectEntry;
import com.zzhalex.slashbladetweaker.definition.BladeDefinition;
import com.zzhalex.slashbladetweaker.resource.BladeResourceBridge;
import java.util.List;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.specialeffect.ISpecialEffect;
import mods.flammpfeil.slashblade.specialeffect.SpecialEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

final class BladeStackFactory {
    private final BladeResourceBridge resourceBridge;

    BladeStackFactory(BladeResourceBridge resourceBridge) {
        this.resourceBridge = resourceBridge;
    }

    ItemStack create(BladeDefinition definition) {
        ensureBladeItemIsNamed(definition.getId());
        ItemStack stack = new ItemStack(SlashBlade.bladeNamed, 1, 0);
        NBTTagCompound tag = new NBTTagCompound();
        setSpecialAttack(tag, definition.getSpecialAttack());
        setSpecialEffects(tag, definition.getSpecialEffects());
        stack.setTagCompound(tag);
        return stack;
    }

    private void ensureBladeItemIsNamed(String bladeId) {
        if (SlashBlade.bladeNamed.getRegistryName() == null) {
            SlashBlade.bladeNamed.setRegistryName(new ResourceLocation(bladeId));
        }
    }

    private void setSpecialAttack(NBTTagCompound tag, Object specialAttack) {
        if (specialAttack instanceof Number) {
            tag.setInteger("SpecialAttackType", ((Number) specialAttack).intValue());
        }
    }

    private void setSpecialEffects(NBTTagCompound tag, List<SpecialEffectEntry> specialEffects) {
        if (specialEffects.isEmpty()) {
            return;
        }
        NBTTagCompound effectTag = new NBTTagCompound();
        for (SpecialEffectEntry entry : specialEffects) {
            Object specialEffect = entry.getSpecialEffect();
            if (specialEffect instanceof ISpecialEffect) {
                effectTag.setInteger(((ISpecialEffect) specialEffect).getEffectKey(), entry.getRequiredLevel());
            } else {
                effectTag.setInteger(String.valueOf(specialEffect), entry.getRequiredLevel());
            }
        }
        tag.setTag("SB.SEffect", effectTag);
    }
}