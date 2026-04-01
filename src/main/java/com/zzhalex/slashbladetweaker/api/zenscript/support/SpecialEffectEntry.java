package com.zzhalex.slashbladetweaker.api.zenscript.support;

public class SpecialEffectEntry {
    private final Object specialEffect;
    private final int requiredLevel;

    public SpecialEffectEntry(Object specialEffect, int requiredLevel) {
        this.specialEffect = specialEffect;
        this.requiredLevel = requiredLevel;
    }

    public Object getSpecialEffect() {
        return specialEffect;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
