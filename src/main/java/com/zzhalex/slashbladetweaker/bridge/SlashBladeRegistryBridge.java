package com.zzhalex.slashbladetweaker.bridge;

import com.zzhalex.slashbladetweaker.definition.BladeDefinition;
import com.zzhalex.slashbladetweaker.definition.BladeFamilyDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedEntityDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialAttackDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialEffectDefinition;
import com.zzhalex.slashbladetweaker.registry.PendingBladeRegistry;
import com.zzhalex.slashbladetweaker.resource.BladeResourceBridge;
import com.zzhalex.slashbladetweaker.runtime.entity.ScriptedEntityRegistry;
import com.zzhalex.slashbladetweaker.runtime.permission.ScriptPermissionService;
import com.zzhalex.slashbladetweaker.runtime.sa.ScriptedSpecialAttack;
import com.zzhalex.slashbladetweaker.runtime.sa.ScriptedSpecialAttackRegistry;
import com.zzhalex.slashbladetweaker.runtime.scheduler.ScriptScheduler;
import com.zzhalex.slashbladetweaker.runtime.se.ScriptedSpecialEffect;
import com.zzhalex.slashbladetweaker.runtime.se.ScriptedSpecialEffectRegistry;
import com.zzhalex.slashbladetweaker.runtime.state.ScriptStateStore;
import com.zzhalex.slashbladetweaker.runtime.unsafe.UnsafeAccessBridge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mods.flammpfeil.slashblade.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.named.NamedBladeManager;
import mods.flammpfeil.slashblade.specialeffect.ISpecialEffect;
import net.minecraft.item.ItemStack;

public class SlashBladeRegistryBridge {
    public interface BladeRegistrationSink {
        void registerBlade(String bladeId, ItemStack stack);

        void registerCreativeEntry(String bladeId, ItemStack stack);

        void registerNamedSoul(String bladeId, ItemStack stack);
    }

    @FunctionalInterface
    public interface AwakeRecipeRegistrar {
        void register(Map<String, BladeFamilyDefinition> families, Map<String, ItemStack> bladeStacks);
    }

    private final PendingBladeRegistry pendingRegistry;
    private final ScriptPermissionService permissionService;
    private final BladeRegistrationSink bladeRegistrationSink;
    private final AwakeRecipeRegistrar awakeRecipeRegistrar;
    private final BladeResourceBridge resourceBridge;
    private final BladeStackFactory bladeStackFactory;
    private final ScriptScheduler scheduler;
    private final ScriptStateStore stateStore;
    private final com.zzhalex.slashbladetweaker.api.zenscript.context.world.WorldAccess worldAccess;
    private final UnsafeAccessBridge unsafeAccessBridge;
    private final Map<String, ScriptedSpecialAttack> specialAttacksById = new LinkedHashMap<>();
    private final Map<String, ScriptedSpecialEffect> specialEffectsById = new LinkedHashMap<>();
    private final Map<String, ItemStack> bladeStacksById = new LinkedHashMap<>();
    private ScriptedEntityRegistry scriptedEntityRegistry;
    private ScriptedSpecialAttackRegistry scriptedSpecialAttackRegistry;
    private ScriptedSpecialEffectRegistry scriptedSpecialEffectRegistry;

    public SlashBladeRegistryBridge(PendingBladeRegistry pendingRegistry, ScriptPermissionService permissionService) {
        this(pendingRegistry, permissionService, new DefaultBladeRegistrationSink(), (families, bladeStacks) -> {
        });
    }

    public SlashBladeRegistryBridge(
            PendingBladeRegistry pendingRegistry,
            ScriptPermissionService permissionService,
            BladeRegistrationSink bladeRegistrationSink,
            AwakeRecipeRegistrar awakeRecipeRegistrar
    ) {
        this(
                pendingRegistry,
                permissionService,
                bladeRegistrationSink,
                awakeRecipeRegistrar,
                new BladeResourceBridge(),
                new ScriptScheduler(),
                new ScriptStateStore(),
                new com.zzhalex.slashbladetweaker.api.zenscript.context.world.WorldAccess(null, null, null, null, null),
                new UnsafeAccessBridge()
        );
    }

    SlashBladeRegistryBridge(
            PendingBladeRegistry pendingRegistry,
            ScriptPermissionService permissionService,
            BladeRegistrationSink bladeRegistrationSink,
            AwakeRecipeRegistrar awakeRecipeRegistrar,
            BladeResourceBridge resourceBridge,
            ScriptScheduler scheduler,
            ScriptStateStore stateStore,
            com.zzhalex.slashbladetweaker.api.zenscript.context.world.WorldAccess worldAccess,
            UnsafeAccessBridge unsafeAccessBridge
    ) {
        this.pendingRegistry = Objects.requireNonNull(pendingRegistry, "pendingRegistry");
        this.permissionService = Objects.requireNonNull(permissionService, "permissionService");
        this.bladeRegistrationSink = Objects.requireNonNull(bladeRegistrationSink, "bladeRegistrationSink");
        this.awakeRecipeRegistrar = Objects.requireNonNull(awakeRecipeRegistrar, "awakeRecipeRegistrar");
        this.resourceBridge = Objects.requireNonNull(resourceBridge, "resourceBridge");
        this.bladeStackFactory = new BladeStackFactory(this.resourceBridge);
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        this.worldAccess = Objects.requireNonNull(worldAccess, "worldAccess");
        this.unsafeAccessBridge = Objects.requireNonNull(unsafeAccessBridge, "unsafeAccessBridge");
    }

    public final void apply() {
        configurePermissionState();
        registerSpecialAttacks();
        registerSpecialEffects();
        registerScriptedEntities();
        registerBladeResources();
        emitBladeStacks();
        registerCreativeEntriesAndNamedSouls();
        registerAwakeRecipes();
    }

    protected void configurePermissionState() {
        scriptedEntityRegistry = new ScriptedEntityRegistry();
        scriptedSpecialAttackRegistry = new ScriptedSpecialAttackRegistry(
                permissionService,
                scheduler,
                stateStore,
                worldAccess,
                scriptedEntityRegistry,
                unsafeAccessBridge
        );
        scriptedSpecialEffectRegistry = new ScriptedSpecialEffectRegistry(
                permissionService,
                scheduler,
                worldAccess,
                scriptedEntityRegistry,
                unsafeAccessBridge
        );
    }

    protected void registerSpecialAttacks() {
        for (ScriptedSpecialAttackDefinition definition : pendingRegistry.getSpecialAttacks().values()) {
            scriptedSpecialAttackRegistry.register(definition);
            ScriptedSpecialAttack runtime = scriptedSpecialAttackRegistry.getById(definition.getId());
            specialAttacksById.put(definition.getId(), runtime);
            ItemSlashBlade.specialAttacks.put(runtime.getNumericId(), runtime);
        }
    }

    protected void registerSpecialEffects() {
        for (ScriptedSpecialEffectDefinition definition : pendingRegistry.getSpecialEffects().values()) {
            scriptedSpecialEffectRegistry.register(definition);
            specialEffectsById.put(definition.getId(), scriptedSpecialEffectRegistry.getById(definition.getId()));
        }
    }

    protected void registerScriptedEntities() {
        for (ScriptedEntityDefinition definition : pendingRegistry.getScriptedEntities().values()) {
            scriptedEntityRegistry.register(definition);
        }
    }

    protected void registerBladeResources() {
        for (BladeDefinition definition : pendingRegistry.getBlades().values()) {
            resourceBridge.bind(definition.getId(), definition.getTextureLocation(), definition.getModelLocation());
        }
    }

    protected void emitBladeStacks() {
        for (BladeDefinition definition : pendingRegistry.getBlades().values()) {
            BladeDefinition resolvedDefinition = resolveBladeDefinition(definition);
            ItemStack stack = bladeStackFactory.create(resolvedDefinition);
            bladeStacksById.put(definition.getId(), stack);
            bladeRegistrationSink.registerBlade(definition.getId(), stack);
        }
    }

    protected void registerCreativeEntriesAndNamedSouls() {
        for (BladeDefinition definition : pendingRegistry.getBlades().values()) {
            ItemStack stack = bladeStacksById.get(definition.getId());
            if (stack == null) {
                continue;
            }
            if (Boolean.TRUE.equals(definition.getAddToCreativeTab())) {
                bladeRegistrationSink.registerCreativeEntry(definition.getId(), stack);
            }
            if (Boolean.TRUE.equals(definition.getRegisterNamedSoul())) {
                bladeRegistrationSink.registerNamedSoul(definition.getId(), stack);
            }
        }
    }

    protected void registerAwakeRecipes() {
        awakeRecipeRegistrar.register(
                pendingRegistry.getFamilies(),
                Collections.unmodifiableMap(new LinkedHashMap<>(bladeStacksById))
        );
    }

    protected BladeDefinition resolveBladeDefinition(BladeDefinition definition) {
        return new BladeDefinition(
                definition.getId(),
                definition.getDisplayName(),
                definition.getModelLocation(),
                definition.getTextureLocation(),
                definition.getDefaultBewitched(),
                definition.getStandbyRenderType(),
                definition.getSummonedSwordColor(),
                definition.getMaxDamage(),
                definition.getBaseAttack(),
                definition.getAttackAmplifier(),
                definition.getProudSoul(),
                definition.getKillCount(),
                definition.getRepairCount(),
                definition.getDestructable(),
                definition.getBroken(),
                definition.getSealed(),
                definition.getCharged(),
                definition.getNoScabbard(),
                definition.getRepairItem(),
                definition.getRepairOreDict(),
                resolveSpecialAttackReference(definition.getSpecialAttack()),
                definition.getAddToCreativeTab(),
                definition.getRegisterNamedSoul(),
                definition.getTrueBladeId(),
                definition.getEnchantments(),
                resolveSpecialEffects(definition.getSpecialEffects()),
                definition.getRawTagOverrides()
        );
    }

    private Object resolveSpecialAttackReference(Object specialAttack) {
        if (specialAttack instanceof ScriptedSpecialAttackDefinition attackDefinition) {
            ScriptedSpecialAttack runtime = specialAttacksById.get(attackDefinition.getId());
            if (runtime != null) {
                return runtime.getNumericId();
            }
        } else if (specialAttack instanceof String attackId) {
            ScriptedSpecialAttack runtime = specialAttacksById.get(attackId);
            if (runtime != null) {
                return runtime.getNumericId();
            }
        } else if (specialAttack instanceof ScriptedSpecialAttack runtime) {
            return runtime.getNumericId();
        }
        return specialAttack;
    }

    private List<com.zzhalex.slashbladetweaker.api.zenscript.support.SpecialEffectEntry> resolveSpecialEffects(
            List<com.zzhalex.slashbladetweaker.api.zenscript.support.SpecialEffectEntry> specialEffects
    ) {
        List<com.zzhalex.slashbladetweaker.api.zenscript.support.SpecialEffectEntry> resolved = new ArrayList<>();
        for (com.zzhalex.slashbladetweaker.api.zenscript.support.SpecialEffectEntry entry : specialEffects) {
            resolved.add(new com.zzhalex.slashbladetweaker.api.zenscript.support.SpecialEffectEntry(
                    resolveSpecialEffectReference(entry.getSpecialEffect()),
                    entry.getRequiredLevel()
            ));
        }
        return resolved;
    }

    private Object resolveSpecialEffectReference(Object specialEffect) {
        if (specialEffect instanceof ScriptedSpecialEffectDefinition effectDefinition) {
            ScriptedSpecialEffect runtime = specialEffectsById.get(effectDefinition.getId());
            if (runtime != null) {
                return runtime;
            }
        } else if (specialEffect instanceof String effectId) {
            ScriptedSpecialEffect runtime = specialEffectsById.get(effectId);
            if (runtime != null) {
                return runtime;
            }
        } else if (specialEffect instanceof ISpecialEffect effect) {
            return effect;
        }
        return specialEffect;
    }

    private static final class DefaultBladeRegistrationSink implements BladeRegistrationSink {
        @Override
        public void registerBlade(String bladeId, ItemStack stack) {
            SlashBlade.registerCustomItemStack(bladeId, stack);
        }

        @Override
        public void registerCreativeEntry(String bladeId, ItemStack stack) {
            ItemSlashBladeNamed.NamedBlades.add(bladeId);
        }

        @Override
        public void registerNamedSoul(String bladeId, ItemStack stack) {
            if (stack.getTagCompound() == null) {
                throw new IllegalStateException("Blade stack for " + bladeId + " does not have an NBT tag");
            }
            String displayName = stack.getDisplayName() != null ? stack.getDisplayName() : bladeId;
            NamedBladeManager.registerBladeSoul(stack.getTagCompound(), displayName);
        }
    }
}
