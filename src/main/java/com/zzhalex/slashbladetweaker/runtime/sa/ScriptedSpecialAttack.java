package com.zzhalex.slashbladetweaker.runtime.sa;

import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialAttackDefinition;
import java.util.Objects;
import mods.flammpfeil.slashblade.specialattack.SpecialAttackBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ScriptedSpecialAttack extends SpecialAttackBase {
    private final ScriptedSpecialAttackDefinition definition;
    private final ScriptedSpecialAttackRegistry registry;
    private final int numericId;

    ScriptedSpecialAttack(ScriptedSpecialAttackDefinition definition, ScriptedSpecialAttackRegistry registry, int numericId) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.numericId = numericId;
    }

    public int getNumericId() {
        return numericId;
    }

    @Override
    public void doSpacialAttack(ItemStack itemStack, EntityPlayer entityPlayer) {
        registry.startSession(definition);
        if (definition.getOnUse() != null) {
            definition.getOnUse().accept(new Object());
        }
    }

    @Override
    public String toString() {
        return definition.getId();
    }
}
