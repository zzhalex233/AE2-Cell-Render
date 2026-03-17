package com.zzhalex233.ae2cellrender.client.drive.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class DriveModelBakeRegistry {

    private static final Set<ModelResourceLocation> TARGETS = new HashSet<>();

    private DriveModelBakeRegistry() {
    }

    public static void register(ModelResourceLocation target) {
        TARGETS.add(target);
    }

    public static void reset() {
        TARGETS.clear();
    }

    public static Set<ModelResourceLocation> getTargets() {
        return Collections.unmodifiableSet(new HashSet<>(TARGETS));
    }

    public static IBakedModel wrap(ModelResourceLocation location, IBakedModel original) {
        if (location == null || original == null || !TARGETS.contains(location)) {
            return original;
        }
        return new DriveDelegatingBakedModel(original);
    }
}
