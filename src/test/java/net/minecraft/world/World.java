package net.minecraft.world;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class World {
    public final WorldProvider provider = new WorldProvider();

    private boolean remote;
    private final List<String> renderUpdates = new ArrayList<>();

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public void markBlockRangeForRenderUpdate(BlockPos from, BlockPos to) {
        renderUpdates.add(from.toLong() + "->" + to.toLong());
    }

    public List<String> getRenderUpdates() {
        return Collections.unmodifiableList(renderUpdates);
    }

    public void clearRenderUpdates() {
        renderUpdates.clear();
    }
}