package com.zzhalex.slashbladetweaker.config;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ScriptPermissionLevelTest {
    @Test
    void parsesSafe() {
        assertSame(ScriptPermissionLevel.SAFE, ScriptPermissionLevel.fromConfigValue("SAFE"));
    }

    @Test
    void parsesExtended() {
        assertSame(ScriptPermissionLevel.EXTENDED, ScriptPermissionLevel.fromConfigValue("EXTENDED"));
    }

    @Test
    void parsesUnsafe() {
        assertSame(ScriptPermissionLevel.UNSAFE, ScriptPermissionLevel.fromConfigValue("UNSAFE"));
    }

    @Test
    void defaultsToExtendedWhenConfigIsAbsentOrInvalid() {
        assertSame(ScriptPermissionLevel.EXTENDED, ScriptPermissionLevel.fromConfigValue(null));
        assertSame(ScriptPermissionLevel.EXTENDED, ScriptPermissionLevel.fromConfigValue(""));
        assertSame(ScriptPermissionLevel.EXTENDED, ScriptPermissionLevel.fromConfigValue("not-a-real-value"));
    }
}
