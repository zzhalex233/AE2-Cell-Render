package net.minecraft.nbt;

import java.util.LinkedHashMap;
import java.util.Map;

public class NBTTagCompound {
    private final Map<String, Object> values = new LinkedHashMap<>();

    public void setString(String key, String value) {
        values.put(key, value);
    }

    public void setInteger(String key, int value) {
        values.put(key, Integer.valueOf(value));
    }

    public void setTag(String key, NBTTagCompound value) {
        values.put(key, value);
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value instanceof String ? (String) value : "";
    }

    public int getInteger(String key) {
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    public NBTTagCompound getCompoundTag(String key) {
        Object value = values.get(key);
        return value instanceof NBTTagCompound ? (NBTTagCompound) value : new NBTTagCompound();
    }

    public NBTTagCompound copy() {
        NBTTagCompound copy = new NBTTagCompound();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof NBTTagCompound) {
                copy.values.put(entry.getKey(), ((NBTTagCompound) value).copy());
            } else {
                copy.values.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}