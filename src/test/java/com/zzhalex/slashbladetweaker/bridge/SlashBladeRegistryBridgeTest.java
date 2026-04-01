package com.zzhalex.slashbladetweaker.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zzhalex.slashbladetweaker.api.zenscript.support.SpecialEffectEntry;
import com.zzhalex.slashbladetweaker.config.ScriptPermissionLevel;
import com.zzhalex.slashbladetweaker.definition.BladeDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedEntityDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialAttackDefinition;
import com.zzhalex.slashbladetweaker.definition.ScriptedSpecialEffectDefinition;
import com.zzhalex.slashbladetweaker.registry.PendingBladeRegistry;
import com.zzhalex.slashbladetweaker.runtime.permission.ScriptPermissionService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mods.flammpfeil.slashblade.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.named.NamedBladeManager;
import mods.flammpfeil.slashblade.specialeffect.SpecialEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SlashBladeRegistryBridgeTest {
    private ItemSlashBladeNamed originalBladeNamed;

    @BeforeEach
    void resetSlashBladeState() {
        originalBladeNamed = SlashBlade.bladeNamed;
        SlashBlade.bladeNamed = new ItemSlashBladeNamed(Item.ToolMaterial.IRON, 4.0F);
        SlashBlade.customItemStacks.clear();
        ItemSlashBlade.textureMap.clear();
        ItemSlashBlade.modelMap.clear();
        ItemSlashBlade.specialAttacks.clear();
        ItemSlashBladeNamed.NamedBlades.clear();
        NamedBladeManager.keyList.clear();
        NamedBladeManager.namedbladeSouls.clear();
        SpecialEffects.reset();
    }

    @AfterEach
    void restoreSlashBladeState() {
        SlashBlade.bladeNamed = originalBladeNamed;
        SlashBlade.customItemStacks.clear();
        ItemSlashBlade.textureMap.clear();
        ItemSlashBlade.modelMap.clear();
        ItemSlashBlade.specialAttacks.clear();
        ItemSlashBladeNamed.NamedBlades.clear();
        NamedBladeManager.keyList.clear();
        NamedBladeManager.namedbladeSouls.clear();
        SpecialEffects.reset();
    }

    @Test
    void registersQueuedDefinitionsInSlashBladeLifecycleOrder() {
        PendingBladeRegistry pendingRegistry = new PendingBladeRegistry();
        pendingRegistry.registerSpecialAttack(new ScriptedSpecialAttackDefinition("slashbladetweaker:judgement"));
        pendingRegistry.registerSpecialEffect(new ScriptedSpecialEffectDefinition("slashbladetweaker:void_edge"));
        pendingRegistry.registerScriptedEntity(new ScriptedEntityDefinition("slashbladetweaker:summoned_helper"));
        pendingRegistry.registerBlade(bladeDefinition());

        RecordingSlashBladeRegistryBridge bridge = new RecordingSlashBladeRegistryBridge(
                pendingRegistry,
                new ScriptPermissionService(ScriptPermissionLevel.UNSAFE)
        );

        bridge.registerQueuedContent();

        assertEquals(java.util.Arrays.asList(
                "permissions",
                "specialAttacks",
                "specialEffects",
                "scriptedEntities",
                "bladeResources",
                "bladeStacks",
                "creativeEntriesAndNamedSouls",
                "awakeRecipes"
        ), bridge.getPhases());

        ItemStack stack = SlashBlade.findItemStack("slashbladetweaker:crimson_moon");
        assertNotNull(stack);
        assertNotNull(stack.getTagCompound());
        assertEquals(10, stack.getTagCompound().getInteger("SpecialAttackType"));
        assertEquals(20, stack.getTagCompound().getCompoundTag("SB.SEffect").getInteger("slashbladetweaker:void_edge"));

        assertTrue(ItemSlashBlade.specialAttacks.containsKey(10));
        assertNotNull(ItemSlashBlade.specialAttacks.get(10));
        assertNotNull(SpecialEffects.getEffect("slashbladetweaker:void_edge"));
        assertTrue(ItemSlashBladeNamed.NamedBlades.contains("slashbladetweaker:crimson_moon"));
        assertTrue(NamedBladeManager.namedbladeSouls.containsKey("slashbladetweaker:crimson_moon"));
        assertNotNull(bridge.getScriptedEntityRegistry().createRuntime("slashbladetweaker:summoned_helper"));
    }

    @Test
    void postInitDelegatesToTheRegistryBridge() throws IOException {
        String source = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/java/com/zzhalex/slashbladetweaker/SlashBladeTweaker.java")), java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(source.contains("SlashBladeRegistryBridge"));
        assertTrue(source.contains("postInit"));
        assertTrue(source.contains("registerQueuedContent"));
    }

    private static BladeDefinition bladeDefinition() {
        return new BladeDefinition(
                "slashbladetweaker:crimson_moon",
                "Crimson Moon",
                "slashbladetweaker:model/blades/crimson_moon.obj",
                "slashbladetweaker:model/blades/crimson_moon.png",
                null,
                null,
                null,
                null,
                7.5D,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "slashbladetweaker:judgement",
                true,
                true,
                null,
                Collections.emptyList(),
                java.util.Collections.singletonList(new SpecialEffectEntry("slashbladetweaker:void_edge", 20)),
                Collections.emptyMap()
        );
    }

    private static final class RecordingSlashBladeRegistryBridge extends SlashBladeRegistryBridge {
        private final List<String> phases = new ArrayList<>();

        private RecordingSlashBladeRegistryBridge(
                PendingBladeRegistry pendingRegistry,
                ScriptPermissionService permissionService
        ) {
            super(pendingRegistry, permissionService);
        }

        @Override
        protected void configurePermissionState() {
            phases.add("permissions");
            super.configurePermissionState();
        }

        @Override
        protected void registerSpecialAttacks() {
            phases.add("specialAttacks");
            super.registerSpecialAttacks();
        }

        @Override
        protected void registerSpecialEffects() {
            phases.add("specialEffects");
            super.registerSpecialEffects();
        }

        @Override
        protected void registerScriptedEntities() {
            phases.add("scriptedEntities");
            super.registerScriptedEntities();
        }

        @Override
        protected void registerBladeResources() {
            phases.add("bladeResources");
            super.registerBladeResources();
        }

        @Override
        protected void emitBladeStacks() {
            phases.add("bladeStacks");
            super.emitBladeStacks();
        }

        @Override
        protected void registerCreativeEntriesAndNamedSouls() {
            phases.add("creativeEntriesAndNamedSouls");
            super.registerCreativeEntriesAndNamedSouls();
        }

        @Override
        protected void registerAwakeRecipes() {
            phases.add("awakeRecipes");
            super.registerAwakeRecipes();
        }

        private List<String> getPhases() {
            return phases;
        }
    }
}

