package com.zzhalex.slashbladetweaker.runtime.sa;

import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialAttackDefinition;
import com.zzhalex.slashbladetweaker.runtime.entity.ScriptedEntityRegistry;
import com.zzhalex.slashbladetweaker.runtime.permission.ScriptPermissionService;
import com.zzhalex.slashbladetweaker.runtime.scheduler.ScriptScheduler;
import com.zzhalex.slashbladetweaker.runtime.state.ScriptStateStore;
import com.zzhalex.slashbladetweaker.runtime.unsafe.UnsafeAccessBridge;
import com.zzhalex.slashbladetweaker.api.zenscript.context.world.WorldAccess;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScriptedSpecialAttackRegistry {
    private final Map<String, ScriptedSpecialAttack> attacksById = new LinkedHashMap<>();
    private final List<Session> sessions = new ArrayList<>();
    private int nextNumericId = 10;

    public ScriptedSpecialAttackRegistry() {
    }

    public ScriptedSpecialAttackRegistry(
            ScriptPermissionService permissionService,
            ScriptScheduler scheduler,
            ScriptStateStore stateStore,
            WorldAccess worldAccess,
            ScriptedEntityRegistry scriptedEntityRegistry,
            UnsafeAccessBridge unsafeAccessBridge
    ) {
        this();
    }

    public void register(ScriptedSpecialAttackDefinition definition) {
        attacksById.put(definition.getId(), new ScriptedSpecialAttack(definition, this, nextNumericId));
        nextNumericId += 10;
    }

    public ScriptedSpecialAttack getById(String id) {
        return attacksById.get(id);
    }

    public ScriptedSpecialAttack create(String id) {
        return attacksById.get(id);
    }

    void startSession(ScriptedSpecialAttackDefinition definition) {
        sessions.add(new Session(definition));
    }

    public void tickSessions() {
        Iterator<Session> iterator = sessions.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick()) {
                iterator.remove();
            }
        }
    }

    public Map<String, ScriptedSpecialAttack> getAttacksById() {
        return Collections.unmodifiableMap(attacksById);
    }

    private static final class Session {
        private final ScriptedSpecialAttackDefinition definition;
        private boolean ticked;

        private Session(ScriptedSpecialAttackDefinition definition) {
            this.definition = definition;
        }

        private boolean tick() {
            if (!ticked) {
                ticked = true;
                if (definition.getOnTick() != null) {
                    definition.getOnTick().accept(new Object());
                }
                return false;
            }
            if (definition.getOnFinish() != null) {
                definition.getOnFinish().accept(new Object());
            }
            return true;
        }
    }
}
