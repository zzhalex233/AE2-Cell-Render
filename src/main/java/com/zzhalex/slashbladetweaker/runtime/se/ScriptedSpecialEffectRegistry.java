package com.zzhalex.slashbladetweaker.runtime.se;

import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialEffectDefinition;
import com.zzhalex.slashbladetweaker.runtime.entity.ScriptedEntityRegistry;
import com.zzhalex.slashbladetweaker.runtime.permission.ScriptPermissionService;
import com.zzhalex.slashbladetweaker.runtime.scheduler.ScriptScheduler;
import com.zzhalex.slashbladetweaker.runtime.unsafe.UnsafeAccessBridge;
import com.zzhalex.slashbladetweaker.api.zenscript.context.world.WorldAccess;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import mods.flammpfeil.slashblade.specialeffect.SpecialEffects;

public class ScriptedSpecialEffectRegistry {
    private final Map<String, ScriptedSpecialEffect> effectsById = new LinkedHashMap<>();

    public ScriptedSpecialEffectRegistry() {
    }

    public ScriptedSpecialEffectRegistry(
            ScriptPermissionService permissionService,
            ScriptScheduler scheduler,
            WorldAccess worldAccess,
            ScriptedEntityRegistry scriptedEntityRegistry,
            UnsafeAccessBridge unsafeAccessBridge
    ) {
        this();
    }

    public void register(ScriptedSpecialEffectDefinition definition) {
        ScriptedSpecialEffect effect = new ScriptedSpecialEffect(definition);
        effectsById.put(definition.getId(), effect);
        SpecialEffects.register(effect);
    }

    public ScriptedSpecialEffect getById(String id) {
        return effectsById.get(id);
    }

    public Map<String, ScriptedSpecialEffect> getEffectsById() {
        return Collections.unmodifiableMap(effectsById);
    }
}
