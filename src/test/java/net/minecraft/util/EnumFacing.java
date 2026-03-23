package net.minecraft.util;

public enum EnumFacing {
    NORTH,
    SOUTH,
    EAST,
    WEST,
    UP,
    DOWN;

    public static EnumFacing getFacingFromVector(float x, float y, float z) {
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        float absZ = Math.abs(z);
        if (absX >= absY && absX >= absZ) {
            return x >= 0.0F ? EAST : WEST;
        }
        if (absY >= absX && absY >= absZ) {
            return y >= 0.0F ? UP : DOWN;
        }
        return z >= 0.0F ? SOUTH : NORTH;
    }
}
