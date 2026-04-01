package com.zzhalex.slashbladetweaker.definition;

import com.zzhalex.slashbladetweaker.api.zenscript.support.SpecialEffectEntry;
import java.util.List;
import java.util.Map;

public class BladeDefinition {
    private final String id;
    private final String displayName;
    private final String modelLocation;
    private final String textureLocation;
    private final Object defaultBewitched;
    private final Object standbyRenderType;
    private final Object summonedSwordColor;
    private final Object maxDamage;
    private final Double baseAttack;
    private final Object attackAmplifier;
    private final Object proudSoul;
    private final Object killCount;
    private final Object repairCount;
    private final Object destructable;
    private final Object broken;
    private final Object sealed;
    private final Object charged;
    private final Object noScabbard;
    private final Object repairItem;
    private final Object repairOreDict;
    private final Object specialAttack;
    private final Boolean addToCreativeTab;
    private final Boolean registerNamedSoul;
    private final Object trueBladeId;
    private final List<Object> enchantments;
    private final List<SpecialEffectEntry> specialEffects;
    private final Map<String, Object> rawTagOverrides;

    public BladeDefinition(
            String id,
            String displayName,
            String modelLocation,
            String textureLocation,
            Object defaultBewitched,
            Object standbyRenderType,
            Object summonedSwordColor,
            Object maxDamage,
            Double baseAttack,
            Object attackAmplifier,
            Object proudSoul,
            Object killCount,
            Object repairCount,
            Object destructable,
            Object broken,
            Object sealed,
            Object charged,
            Object noScabbard,
            Object repairItem,
            Object repairOreDict,
            Object specialAttack,
            Boolean addToCreativeTab,
            Boolean registerNamedSoul,
            Object trueBladeId,
            List<Object> enchantments,
            List<SpecialEffectEntry> specialEffects,
            Map<String, Object> rawTagOverrides
    ) {
        this.id = id;
        this.displayName = displayName;
        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.defaultBewitched = defaultBewitched;
        this.standbyRenderType = standbyRenderType;
        this.summonedSwordColor = summonedSwordColor;
        this.maxDamage = maxDamage;
        this.baseAttack = baseAttack;
        this.attackAmplifier = attackAmplifier;
        this.proudSoul = proudSoul;
        this.killCount = killCount;
        this.repairCount = repairCount;
        this.destructable = destructable;
        this.broken = broken;
        this.sealed = sealed;
        this.charged = charged;
        this.noScabbard = noScabbard;
        this.repairItem = repairItem;
        this.repairOreDict = repairOreDict;
        this.specialAttack = specialAttack;
        this.addToCreativeTab = addToCreativeTab;
        this.registerNamedSoul = registerNamedSoul;
        this.trueBladeId = trueBladeId;
        this.enchantments = enchantments;
        this.specialEffects = specialEffects;
        this.rawTagOverrides = rawTagOverrides;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getModelLocation() { return modelLocation; }
    public String getTextureLocation() { return textureLocation; }
    public Object getDefaultBewitched() { return defaultBewitched; }
    public Object getStandbyRenderType() { return standbyRenderType; }
    public Object getSummonedSwordColor() { return summonedSwordColor; }
    public Object getMaxDamage() { return maxDamage; }
    public Double getBaseAttack() { return baseAttack; }
    public Object getAttackAmplifier() { return attackAmplifier; }
    public Object getProudSoul() { return proudSoul; }
    public Object getKillCount() { return killCount; }
    public Object getRepairCount() { return repairCount; }
    public Object getDestructable() { return destructable; }
    public Object getBroken() { return broken; }
    public Object getSealed() { return sealed; }
    public Object getCharged() { return charged; }
    public Object getNoScabbard() { return noScabbard; }
    public Object getRepairItem() { return repairItem; }
    public Object getRepairOreDict() { return repairOreDict; }
    public Object getSpecialAttack() { return specialAttack; }
    public Boolean getAddToCreativeTab() { return addToCreativeTab; }
    public Boolean getRegisterNamedSoul() { return registerNamedSoul; }
    public Object getTrueBladeId() { return trueBladeId; }
    public List<Object> getEnchantments() { return enchantments; }
    public List<SpecialEffectEntry> getSpecialEffects() { return specialEffects; }
    public Map<String, Object> getRawTagOverrides() { return rawTagOverrides; }
}
