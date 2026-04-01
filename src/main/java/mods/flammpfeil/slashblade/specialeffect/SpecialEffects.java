package mods.flammpfeil.slashblade.specialeffect;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpecialEffects {
    private static final Map<String, ISpecialEffect> EFFECTS = new LinkedHashMap<>();

    public static ISpecialEffect register(ISpecialEffect effect) {
        EFFECTS.put(effect.getEffectKey(), effect);
        return effect;
    }

    public static ISpecialEffect getEffect(String key) {
        return EFFECTS.get(key);
    }

    public static void reset() {
        EFFECTS.clear();
    }
}