package mods.flammpfeil.slashblade.specialeffect;

public interface ISpecialEffect {
    void register();

    int getDefaultRequiredLevel();

    String getEffectKey();
}