package net.minecraft.util.math;

public class BlockPos {
    private final long encoded;

    public BlockPos(long encoded) {
        this.encoded = encoded;
    }

    public static BlockPos fromLong(long encoded) {
        return new BlockPos(encoded);
    }

    public long toLong() {
        return encoded;
    }
}