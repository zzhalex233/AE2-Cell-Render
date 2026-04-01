package com.zzhalex.slashbladetweaker.runtime.se;

import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialEffectDefinition;
import java.util.Objects;
import mods.flammpfeil.slashblade.specialeffect.ISpecialEffect;

public class ScriptedSpecialEffect implements ISpecialEffect {
    private final ScriptedSpecialEffectDefinition definition;

    public ScriptedSpecialEffect(ScriptedSpecialEffectDefinition definition) {
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    @Override
    public void register() {
    }

    @Override
    public int getDefaultRequiredLevel() {
        return 0;
    }

    @Override
    public String getEffectKey() {
        return definition.getId();
    }
}
