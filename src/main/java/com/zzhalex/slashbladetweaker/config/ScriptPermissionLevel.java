package com.zzhalex.slashbladetweaker.config;

import java.util.Locale;

public enum ScriptPermissionLevel {
    SAFE,
    EXTENDED,
    UNSAFE;

    public static ScriptPermissionLevel fromConfigValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return EXTENDED;
        }
        try {
            return ScriptPermissionLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return EXTENDED;
        }
    }
}
