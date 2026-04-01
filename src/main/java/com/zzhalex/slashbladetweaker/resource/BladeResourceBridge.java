package com.zzhalex.slashbladetweaker.resource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BladeResourceBridge {
    private final Map<String, String> textureLocations = new LinkedHashMap<>();
    private final Map<String, String> modelLocations = new LinkedHashMap<>();

    public void bind(String bladeId, String textureLocation, String modelLocation) {
        textureLocations.put(bladeId, textureLocation);
        modelLocations.put(bladeId, modelLocation);
    }

    public Map<String, String> getTextureLocations() {
        return Collections.unmodifiableMap(textureLocations);
    }

    public Map<String, String> getModelLocations() {
        return Collections.unmodifiableMap(modelLocations);
    }
}