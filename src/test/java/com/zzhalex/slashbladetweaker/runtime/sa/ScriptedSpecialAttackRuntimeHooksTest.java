package com.zzhalex.slashbladetweaker.runtime.sa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialAttackDefinition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.junit.jupiter.api.Test;

class ScriptedSpecialAttackRuntimeHooksTest {
    @Test
    void endPhaseServerTicksAdvanceRegisteredSessions() {
        ScriptedSpecialAttackRegistry registry = createRegistry();
        List<String> calls = new ArrayList<>();

        registry.register(new ScriptedSpecialAttackDefinition("slashbladetweaker:lifecycle")
                .onUse(context -> calls.add("use"))
                .onTick(context -> calls.add("tick"))
                .onFinish(context -> calls.add("finish")));

        ScriptedSpecialAttack runtime = registry.create("slashbladetweaker:lifecycle");
        runtime.doSpacialAttack(new ItemStack(new Item(), 1, 0), new EntityPlayer());

        ScriptedSpecialAttackRuntimeHooks hooks = new ScriptedSpecialAttackRuntimeHooks(registry);
        hooks.onServerTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
        hooks.onServerTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));

        assertEquals(java.util.Arrays.asList("use", "tick", "finish"), calls);
    }

    private static ScriptedSpecialAttackRegistry createRegistry() {
        return new ScriptedSpecialAttackRegistry();
    }
}

