package com.zzhalex.slashbladetweaker.registry;

import com.zzhalex.slashbladetweaker.definition.BladeDefinition;
import com.zzhalex.slashbladetweaker.definition.BladeFamilyDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedEntityDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialAttackDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialEffectDefinition;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PendingBladeRegistry {
    private final Map<String, BladeDefinition> blades = new LinkedHashMap<>();
    private final Map<String, BladeFamilyDefinition> families = new LinkedHashMap<>();
    private final Map<String, ScriptedEntityDefinition> scriptedEntities = new LinkedHashMap<>();
    private final Map<String, ScriptedSpecialAttackDefinition> specialAttacks = new LinkedHashMap<>();
    private final Map<String, ScriptedSpecialEffectDefinition> specialEffects = new LinkedHashMap<>();

    public void registerBlade(BladeDefinition definition) {
        blades.put(definition.getId(), definition);
    }

    public void registerFamily(BladeFamilyDefinition definition) {
        families.put(definition.getId(), definition);
    }

    public void registerScriptedEntity(ScriptedEntityDefinition definition) {
        scriptedEntities.put(definition.getId(), definition);
    }

    public void registerSpecialAttack(ScriptedSpecialAttackDefinition definition) {
        specialAttacks.put(definition.getId(), definition);
    }

    public void registerSpecialEffect(ScriptedSpecialEffectDefinition definition) {
        specialEffects.put(definition.getId(), definition);
    }

    public Map<String, BladeDefinition> getBlades() {
        return Collections.unmodifiableMap(blades);
    }

    public Map<String, BladeFamilyDefinition> getFamilies() {
        return Collections.unmodifiableMap(families);
    }

    public Map<String, ScriptedEntityDefinition> getScriptedEntities() {
        return Collections.unmodifiableMap(scriptedEntities);
    }

    public Map<String, ScriptedSpecialAttackDefinition> getSpecialAttacks() {
        return Collections.unmodifiableMap(specialAttacks);
    }

    public Map<String, ScriptedSpecialEffectDefinition> getSpecialEffects() {
        return Collections.unmodifiableMap(specialEffects);
    }
}
