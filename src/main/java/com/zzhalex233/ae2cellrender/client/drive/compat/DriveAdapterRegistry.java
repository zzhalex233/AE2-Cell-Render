package com.zzhalex233.ae2cellrender.client.drive.compat;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DriveAdapterRegistry {

    private static final List<DriveAdapter> ADAPTERS = new ArrayList<>();

    private DriveAdapterRegistry() {
    }

    public static void register(DriveAdapter adapter) {
        ADAPTERS.add(adapter);
    }

    public static void reset() {
        ADAPTERS.clear();
    }

    public static List<DriveAdapter> getRegisteredAdapters() {
        return Collections.unmodifiableList(new ArrayList<>(ADAPTERS));
    }

    public static DriveAdapter findAdapter(Block block, TileEntity tile) {
        for (DriveAdapter adapter : ADAPTERS) {
            if (adapter.matches(block, tile)) {
                return adapter;
            }
        }
        return null;
    }
}
