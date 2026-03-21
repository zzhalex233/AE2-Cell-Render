package com.zzhalex233.ae2cellrender.client.drive;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

public final class CellSeriesKey {
    private static final Pattern SPLIT_CAPACITY_SUFFIX = Pattern.compile("^(.*)_([0-9]+)_([A-Za-z]+)$");
    private static final Pattern COMPACT_CAPACITY_SUFFIX = Pattern.compile("^(.*)_([0-9]+)([A-Za-z]+)$");
    private static final String[] CAPACITY_UNIT_ORDER = {"k", "m", "g", "t", "p", "e", "z", "y"};
    private static final int UNGROUPED_TIER = Integer.MAX_VALUE;

    private final String domain;
    private final String originalPath;
    private final String normalizedPath;
    private final int tierRank;
    private final CapacityKey capacityKey;
    private final boolean grouped;

    private CellSeriesKey(String domain, String originalPath, String normalizedPath, int tierRank, CapacityKey capacityKey, boolean grouped) {
        this.domain = domain;
        this.originalPath = originalPath;
        this.normalizedPath = normalizedPath;
        this.tierRank = tierRank;
        this.capacityKey = capacityKey;
        this.grouped = grouped;
    }

    public static CellSeriesKey from(ResourceLocation registryName) {
        if (registryName == null) {
            return new CellSeriesKey("", "", "", UNGROUPED_TIER, CapacityKey.ungrouped(), false);
        }

        return fromPath(registryName.getNamespace(), registryName.getPath());
    }

    public static CellSeriesKey from(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            return new CellSeriesKey("", "", "", UNGROUPED_TIER, CapacityKey.ungrouped(), false);
        }
        return from(stack.getItem().getRegistryName());
    }

    private static CellSeriesKey fromPath(String domain, String path) {
        if (path == null || path.isEmpty()) {
            return new CellSeriesKey(domain, "", "", UNGROUPED_TIER, CapacityKey.ungrouped(), false);
        }

        CapacityMatch match = matchCapacitySuffix(path);
        if (match == null) {
            return new CellSeriesKey(domain, path, path, UNGROUPED_TIER, CapacityKey.ungrouped(), false);
        }

        if (match.normalizedPath == null || match.normalizedPath.isEmpty()) {
            return new CellSeriesKey(domain, path, path, UNGROUPED_TIER, CapacityKey.ungrouped(), false);
        }

        int tierRank = parsePositiveInt(match.amountText);
        int unitOrder = parseUnitOrder(match.unitText);
        if (tierRank <= 0 || unitOrder < 0) {
            return new CellSeriesKey(domain, path, path, UNGROUPED_TIER, CapacityKey.ungrouped(), false);
        }

        CapacityKey capacityKey = new CapacityKey(unitOrder, tierRank);
        return new CellSeriesKey(domain, path, match.normalizedPath, tierRank, capacityKey, true);
    }

    private static CapacityMatch matchCapacitySuffix(String path) {
        Matcher splitMatcher = SPLIT_CAPACITY_SUFFIX.matcher(path);
        if (splitMatcher.matches()) {
            return new CapacityMatch(splitMatcher.group(1), splitMatcher.group(2), splitMatcher.group(3));
        }

        Matcher compactMatcher = COMPACT_CAPACITY_SUFFIX.matcher(path);
        if (compactMatcher.matches()) {
            return new CapacityMatch(compactMatcher.group(1), compactMatcher.group(2), compactMatcher.group(3));
        }

        return null;
    }

    private static int parsePositiveInt(String amountText) {
        try {
            int value = Integer.parseInt(amountText);
            return value > 0 ? value : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static int parseUnitOrder(String unitText) {
        if (unitText == null || unitText.isEmpty()) {
            return -1;
        }

        String normalizedUnit = unitText.toLowerCase(Locale.ROOT);
        if (normalizedUnit.endsWith("b")) {
            normalizedUnit = normalizedUnit.substring(0, normalizedUnit.length() - 1);
        }
        if (normalizedUnit.length() != 1) {
            return -1;
        }

        char unitPrefix = normalizedUnit.charAt(0);
        for (int index = 0; index < CAPACITY_UNIT_ORDER.length; index++) {
            if (CAPACITY_UNIT_ORDER[index].charAt(0) == unitPrefix) {
                return index;
            }
        }
        return -1;
    }

    public String domain() {
        return domain;
    }

    public String originalPath() {
        return originalPath;
    }

    public String normalizedPath() {
        return normalizedPath;
    }

    public int tierRank() {
        return tierRank;
    }

    CapacityKey capacityKey() {
        return capacityKey;
    }

    public boolean isGrouped() {
        return grouped;
    }

    static final class CapacityKey implements Comparable<CapacityKey> {
        private final int unitOrder;
        private final int amount;

        private CapacityKey(int unitOrder, int amount) {
            this.unitOrder = unitOrder;
            this.amount = amount;
        }

        static CapacityKey ungrouped() {
            return new CapacityKey(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public int compareTo(CapacityKey other) {
            if (other == null) {
                return -1;
            }
            if (unitOrder != other.unitOrder) {
                return Integer.compare(unitOrder, other.unitOrder);
            }
            return Integer.compare(amount, other.amount);
        }
    }

    private static final class CapacityMatch {
        private final String normalizedPath;
        private final String amountText;
        private final String unitText;

        private CapacityMatch(String normalizedPath, String amountText, String unitText) {
            this.normalizedPath = normalizedPath;
            this.amountText = amountText;
            this.unitText = unitText;
        }
    }
}
