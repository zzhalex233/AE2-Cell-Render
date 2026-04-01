package com.zzhalex.slashbladetweaker.runtime.entity;

import com.zzhalex.slashbladetweaker.definition.ScriptedEntityDefinition;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScriptedEntityRegistry {
    private final Map<String, ScriptedEntityRuntime> runtimes = new LinkedHashMap<>();

    public void register(ScriptedEntityDefinition definition) {
        runtimes.put(definition.getId(), new ScriptedEntityRuntime(definition.getId()));
    }

    public ScriptedEntityRuntime createRuntime(String id) {
        return runtimes.get(id);
    }

    public Map<String, ScriptedEntityRuntime> getRuntimes() {
        return Collections.unmodifiableMap(runtimes);
    }

    public static final class ScriptedEntityRuntime {
        private final String id;

        private ScriptedEntityRuntime(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
