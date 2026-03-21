package net.minecraft.nbt;

import java.util.LinkedHashMap;
import java.util.Map;

public class NBTTagCompound {
    private final Map<String, String> values = new LinkedHashMap<>();

    public void setString(String key, String value) {
        values.put(key, value);
    }

    public String getString(String key) {
        return values.getOrDefault(key, "");
    }

    public NBTTagCompound copy() {
        NBTTagCompound copy = new NBTTagCompound();
        copy.values.putAll(values);
        return copy;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
