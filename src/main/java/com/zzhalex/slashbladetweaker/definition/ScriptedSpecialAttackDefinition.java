package com.zzhalex.slashbladetweaker.definition;

import java.util.Objects;
import java.util.function.Consumer;

public class ScriptedSpecialAttackDefinition {
    private final String id;
    private Consumer<Object> onUse;
    private Consumer<Object> onTick;
    private Consumer<Object> onFinish;

    public ScriptedSpecialAttackDefinition(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public String getId() {
        return id;
    }

    public ScriptedSpecialAttackDefinition onUse(Consumer<Object> onUse) {
        this.onUse = onUse;
        return this;
    }

    public ScriptedSpecialAttackDefinition onTick(Consumer<Object> onTick) {
        this.onTick = onTick;
        return this;
    }

    public ScriptedSpecialAttackDefinition onFinish(Consumer<Object> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    public Consumer<Object> getOnUse() {
        return onUse;
    }

    public Consumer<Object> getOnTick() {
        return onTick;
    }

    public Consumer<Object> getOnFinish() {
        return onFinish;
    }
}
