package com.zzhalex233.ae2cellrender.client.drive;

import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;

import java.util.HashMap;
import java.util.Map;

public final class DriveRenderCache {

    private static final DriveRenderCache INSTANCE = new DriveRenderCache();
    private static final long REQUEST_COOLDOWN_TICKS = 20L;

    private final Map<String, Entry> entries = new HashMap<>();

    public static DriveRenderCache getInstance() {
        return INSTANCE;
    }

    public boolean shouldRequest(int dimensionId, long positionKey, int digest, long worldTime) {
        Entry entry = entries.computeIfAbsent(key(dimensionId, positionKey), ignored -> new Entry());
        if (entry.snapshot != null && entry.snapshotDigest == digest) {
            return false;
        }
        if (entry.requestedDigest != digest) {
            entry.requestedDigest = digest;
            entry.lastRequestTick = worldTime;
            return true;
        }
        if (worldTime - entry.lastRequestTick >= REQUEST_COOLDOWN_TICKS) {
            entry.lastRequestTick = worldTime;
            return true;
        }
        return false;
    }

    public void store(DriveRenderSnapshot snapshot, int digest) {
        Entry entry = entries.computeIfAbsent(key(snapshot.getDimensionId(), snapshot.getPositionKey()), ignored -> new Entry());
        entry.snapshot = snapshot;
        entry.snapshotDigest = digest;
        entry.requestedDigest = digest;
    }

    public DriveRenderSnapshot getSnapshot(int dimensionId, long positionKey, int digest) {
        Entry entry = entries.get(key(dimensionId, positionKey));
        if (entry == null || entry.snapshot == null || entry.snapshotDigest != digest) {
            return null;
        }
        return entry.snapshot;
    }

    public void clear() {
        entries.clear();
    }

    private static String key(int dimensionId, long positionKey) {
        return dimensionId + ":" + positionKey;
    }

    private static final class Entry {
        private int requestedDigest = Integer.MIN_VALUE;
        private int snapshotDigest = Integer.MIN_VALUE;
        private long lastRequestTick = Long.MIN_VALUE;
        private DriveRenderSnapshot snapshot;
    }
}
