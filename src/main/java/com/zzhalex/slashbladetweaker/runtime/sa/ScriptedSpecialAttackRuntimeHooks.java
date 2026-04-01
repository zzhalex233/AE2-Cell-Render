package com.zzhalex.slashbladetweaker.runtime.sa;

import java.util.Objects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class ScriptedSpecialAttackRuntimeHooks {
    private final ScriptedSpecialAttackRegistry registry;

    public ScriptedSpecialAttackRuntimeHooks(ScriptedSpecialAttackRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            registry.tickSessions();
        }
    }
}
