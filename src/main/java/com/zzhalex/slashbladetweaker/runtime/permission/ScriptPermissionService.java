package com.zzhalex.slashbladetweaker.runtime.permission;

import com.zzhalex.slashbladetweaker.config.ScriptPermissionLevel;
import java.util.Objects;

public class ScriptPermissionService {
    private final ScriptPermissionLevel level;

    public ScriptPermissionService(ScriptPermissionLevel level) {
        this.level = Objects.requireNonNull(level, "level");
    }

    public ScriptPermissionLevel getLevel() {
        return level;
    }
}
