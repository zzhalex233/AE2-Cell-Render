package mods.flammpfeil.slashblade.named;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;

public class NamedBladeManager {
    public static final List<String> keyList = new ArrayList<>();
    public static final Map<String, String> namedbladeSouls = new LinkedHashMap<>();

    public static void registerBladeSoul(NBTTagCompound tag, String displayName) {
        String key = normalize(displayName);
        if (!keyList.contains(key)) {
            keyList.add(key);
        }
        namedbladeSouls.put(key, displayName);
    }

    private static String normalize(String displayName) {
        if (displayName == null) {
            return "unknown";
        }
        if (displayName.startsWith("item.")) {
            String trimmed = displayName.substring(5);
            int split = trimmed.indexOf('.');
            if (split >= 0) {
                return trimmed.substring(0, split) + ":" + trimmed.substring(split + 1);
            }
        }
        return displayName;
    }
}