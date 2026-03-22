package com.zzhalex233.ae2cellrender.client.drive;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import com.zzhalex233.ae2cellrender.config.AE2CellRenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CellColorResolverTest {

    @AfterEach
    void resetMinecraft() {
        Minecraft.getMinecraft().reset();
        ForgeRegistries.ITEMS.clear();
        CellColorResolver.INSTANCE.clear();
        AE2CellRenderConfig.resetForTests();
    }

    @Test
    void fieryStorageCell16kResolvesThroughCanonical1kColor() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture canonicalFixture = GeneratedCellSpriteFixtures.fieryStorageCell1kFixture();
        GeneratedCellSpriteFixtures.ModelSpriteFixture tierFixture = GeneratedCellSpriteFixtures.fieryStorageCell16kFixture();

        register(Items.FIERY_STORAGE_CELL_1K, canonicalFixture);
        register(Items.FIERY_STORAGE_CELL_16K, tierFixture);

        int resolved = resolveSerialized(new ItemStack(Items.FIERY_STORAGE_CELL_16K));

        assertEquals(CellColorMath.postProcessMainColor(canonicalFixture.bodyColor), resolved);
    }

    @Test
    void canonicalLookupPrefersLowestTierItemInSameSeries() {
        ForgeRegistries.ITEMS.register(Items.FIERY_STORAGE_CELL_1K);
        ForgeRegistries.ITEMS.register(Items.FIERY_STORAGE_CELL_16K);

        assertEquals(Items.FIERY_STORAGE_CELL_1K, CellSeriesRegistryLookup.findCanonicalItem(Items.FIERY_STORAGE_CELL_16K));
    }

    @Test
    void canonicalLookupLeavesUngroupedItemsAlone() {
        ForgeRegistries.ITEMS.register(Items.SOLO_STORAGE_CELL);

        assertEquals(Items.SOLO_STORAGE_CELL, CellSeriesRegistryLookup.findCanonicalItem(Items.SOLO_STORAGE_CELL));
    }

    @Test
    void canonicalLookupDoesNotMergeSimilarPrefixes() {
        Item similarPrefix = new Item().setRegistryName(new ResourceLocation("aeadditions", "fiery_storage_cell_component_16k"));

        ForgeRegistries.ITEMS.register(Items.FIERY_STORAGE_CELL_1K);
        ForgeRegistries.ITEMS.register(similarPrefix);

        assertEquals(similarPrefix, CellSeriesRegistryLookup.findCanonicalItem(similarPrefix));
    }

    @Test
    void fieryStorageCellTiersShareCanonicalColor() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture canonicalFixture = GeneratedCellSpriteFixtures.fieryStorageCell1kFixture();
        GeneratedCellSpriteFixtures.ModelSpriteFixture tierFixture = GeneratedCellSpriteFixtures.fieryStorageCell16kFixture();

        register(Items.FIERY_STORAGE_CELL_1K, canonicalFixture);
        register(Items.FIERY_STORAGE_CELL_16K, tierFixture);

        int canonicalResolved = resolveSerialized(new ItemStack(Items.FIERY_STORAGE_CELL_1K));
        int tierResolved = resolveSerialized(new ItemStack(Items.FIERY_STORAGE_CELL_16K));

        assertEquals(CellColorMath.postProcessMainColor(canonicalFixture.bodyColor), canonicalResolved);
        assertEquals(canonicalResolved, tierResolved);
    }

    @Test
    void nonTieredItemKeepsItsOwnColor() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture soloFixture = GeneratedCellSpriteFixtures.soloStorageCellFixture();

        register(Items.SOLO_STORAGE_CELL, soloFixture);

        int resolved = resolveSerialized(new ItemStack(Items.SOLO_STORAGE_CELL));

        assertEquals(CellColorMath.postProcessMainColor(soloFixture.bodyColor), resolved);
    }

    @Test
    void canonicalLookupPreservesMetadataForTintedModels() {
        registerTinted(Items.FIERY_STORAGE_CELL_1K);
        Minecraft.getMinecraft().getItemColors().setColorMultiplier((stack, tintIndex) -> stack.getMetadata() == 7 ? 0xFF44CC88 : 0xFFCC4444);

        ForgeRegistries.ITEMS.register(Items.FIERY_STORAGE_CELL_16K);

        NBTTagCompound greenTag = new NBTTagCompound();
        greenTag.setString("value", "preserved");
        ItemStack greenTieredStack = new ItemStack(Items.FIERY_STORAGE_CELL_16K, 1, 7, greenTag);

        NBTTagCompound redTag = new NBTTagCompound();
        redTag.setString("value", "other");
        ItemStack redTieredStack = new ItemStack(Items.FIERY_STORAGE_CELL_16K, 1, 2, redTag);

        int resolved = resolveSerialized(greenTieredStack);
        int secondResolved = resolveSerialized(redTieredStack);

        assertEquals(0xFF44CC88, resolved);
        assertEquals(0xFFCC4444, secondResolved);
    }

    @Test
    void metadataTieredCellUsesCanonicalVariantColorFromSameRegisteredItem() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture canonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xC8, 0xA1, 0x5A),
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xD8, 0x2C)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture tierFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xD2, 0xA9, 0x62),
                GeneratedCellSpriteFixtures.opaque(0xD7, 0x37, 0x2A)
        );

        MetadataTieredCellItem tieredItem = new MetadataTieredCellItem(0, 3);
        ForgeRegistries.ITEMS.register(tieredItem);
        Minecraft.getMinecraft().getRenderItem().setModelResolver(tieredItem, stack -> {
            GeneratedCellSpriteFixtures.ModelSpriteFixture fixture = stack.getMetadata() == 0 ? canonicalFixture : tierFixture;
            return new FixtureBakedModel(fixture.quads, fixture.bodySprite);
        });

        int resolved = resolveSerialized(new ItemStack(tieredItem, 1, 3));

        assertEquals(CellColorMath.postProcessMainColor(canonicalFixture.bodyColor), resolved);
    }

    @Test
    void metadataFamiliesSplitWhenVariantColorsAreFarApart() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture paleCanonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xEC, 0xEB, 0xE6),
                GeneratedCellSpriteFixtures.opaque(0xC6, 0xC4, 0xBC)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture paleTierFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xDD, 0xDB, 0xD4),
                GeneratedCellSpriteFixtures.opaque(0xB8, 0xB4, 0xAB)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture redCanonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xC7, 0x4B, 0x43),
                GeneratedCellSpriteFixtures.opaque(0x8C, 0x19, 0x14)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture redTierFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xB1, 0x31, 0x2A),
                GeneratedCellSpriteFixtures.opaque(0x78, 0x10, 0x0B)
        );

        MetadataTieredCellItem tieredItem = new MetadataTieredCellItem(0, 1, 2, 3);
        ForgeRegistries.ITEMS.register(tieredItem);
        Minecraft.getMinecraft().getRenderItem().setModelResolver(tieredItem, stack -> {
            GeneratedCellSpriteFixtures.ModelSpriteFixture fixture;
            switch (stack.getMetadata()) {
                case 0:
                    fixture = paleCanonicalFixture;
                    break;
                case 1:
                    fixture = paleTierFixture;
                    break;
                case 2:
                    fixture = redCanonicalFixture;
                    break;
                default:
                    fixture = redTierFixture;
                    break;
            }
            return new FixtureBakedModel(fixture.quads, fixture.bodySprite);
        });

        int paleResolved = resolveSerialized(new ItemStack(tieredItem, 1, 1));
        int redResolved = resolveSerialized(new ItemStack(tieredItem, 1, 3));

        assertEquals(CellColorMath.postProcessMainColor(paleCanonicalFixture.bodyColor), paleResolved);
        assertEquals(CellColorMath.postProcessMainColor(redCanonicalFixture.bodyColor), redResolved);
    }

    @Test
    void metadataFamiliesAlsoApplyToAeAdditionsStorageRegistryNames() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture canonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xC8, 0xA1, 0x5A),
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xD8, 0x2C)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture tierFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xD2, 0xA9, 0x62),
                GeneratedCellSpriteFixtures.opaque(0xD7, 0x37, 0x2A)
        );

        MetadataTieredCellItem tieredItem = new MetadataTieredCellItem("storage.gas", 0, 3);
        ForgeRegistries.ITEMS.register(tieredItem);
        Minecraft.getMinecraft().getRenderItem().setModelResolver(tieredItem, stack -> {
            GeneratedCellSpriteFixtures.ModelSpriteFixture fixture = stack.getMetadata() == 0 ? canonicalFixture : tierFixture;
            return new FixtureBakedModel(fixture.quads, fixture.bodySprite);
        });

        int resolved = resolveSerialized(new ItemStack(tieredItem, 1, 3));

        assertEquals(CellColorMath.postProcessMainColor(canonicalFixture.bodyColor), resolved);
    }

    @Test
    void metadataFamiliesKeepCloseGasStorageTiersInOneSeries() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture oneKFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xC5, 0xC9, 0x5E),
                GeneratedCellSpriteFixtures.opaque(0x34, 0xD5, 0xDA)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture sixtyFourKFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xDA, 0xDC, 0x99),
                GeneratedCellSpriteFixtures.opaque(0x34, 0xD5, 0xDA)
        );

        MetadataTieredCellItem tieredItem = new MetadataTieredCellItem("storage.gas", 0, 3);
        ForgeRegistries.ITEMS.register(tieredItem);
        Minecraft.getMinecraft().getRenderItem().setModelResolver(tieredItem, stack -> {
            GeneratedCellSpriteFixtures.ModelSpriteFixture fixture = stack.getMetadata() == 0 ? oneKFixture : sixtyFourKFixture;
            return new FixtureBakedModel(fixture.quads, fixture.bodySprite);
        });

        int resolved = resolveSerialized(new ItemStack(tieredItem, 1, 3));

        assertEquals(CellColorMath.postProcessMainColor(oneKFixture.bodyColor), resolved);
    }

    @Test
    void configCanDisableMetadataFamilySplitting() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture canonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xC8, 0xA1, 0x5A),
                GeneratedCellSpriteFixtures.opaque(0xFF, 0xD8, 0x2C)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture tierFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xD2, 0xA9, 0x62),
                GeneratedCellSpriteFixtures.opaque(0xD7, 0x37, 0x2A)
        );

        MetadataTieredCellItem tieredItem = new MetadataTieredCellItem("storage.gas", 0, 3);
        ForgeRegistries.ITEMS.register(tieredItem);
        Minecraft.getMinecraft().getRenderItem().setModelResolver(tieredItem, stack -> {
            GeneratedCellSpriteFixtures.ModelSpriteFixture fixture = stack.getMetadata() == 0 ? canonicalFixture : tierFixture;
            return new FixtureBakedModel(fixture.quads, fixture.bodySprite);
        });

        AE2CellRenderConfig.overrideEnableSeriesColorFamiliesForTests(false);
        CellColorResolver.INSTANCE.clear();

        int canonicalResolved = resolveSerialized(new ItemStack(tieredItem, 1, 0));
        int tierResolved = resolveSerialized(new ItemStack(tieredItem, 1, 3));

        assertEquals(CellColorMath.postProcessMainColor(canonicalFixture.bodyColor), canonicalResolved);
        assertEquals(canonicalResolved, tierResolved);
    }

    @Test
    void registryFamiliesKeepSameHueBlueGasCellsInOneSeries() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture oneKFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xA8, 0xC6, 0xE9),
                GeneratedCellSpriteFixtures.opaque(0xF4, 0x92, 0xC0)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture sixtyFourKFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0x4C, 0xB0, 0xD8),
                GeneratedCellSpriteFixtures.opaque(0xF4, 0x92, 0xC0)
        );

        Item oneK = registerSeriesItem("mekeng", "gas_cell_1k", oneKFixture);
        Item sixtyFourK = registerSeriesItem("mekeng", "gas_cell_64k", sixtyFourKFixture);

        int oneKResolved = resolveSerialized(new ItemStack(oneK));
        int sixtyFourKResolved = resolveSerialized(new ItemStack(sixtyFourK));

        assertEquals(CellColorMath.postProcessMainColor(oneKFixture.bodyColor), oneKResolved);
        assertEquals(oneKResolved, sixtyFourKResolved);
    }

    @Test
    void loweringHueThresholdSplitsNearbyBlueAndCyanFamilies() {
        Item oneK = registerSeriesItem("mekeng", "gas_cell_1k", 0xFFA8C6E9);
        Item sixtyFourK = registerSeriesItem("mekeng", "gas_cell_64k", 0xFF4CB0D8);

        AE2CellRenderConfig.overrideFamilyHueThresholdForTests(5.0F);
        CellColorResolver.INSTANCE.clear();

        int oneKResolved = resolveSerialized(new ItemStack(oneK));
        int sixtyFourKResolved = resolveSerialized(new ItemStack(sixtyFourK));

        assertNotEquals(oneKResolved, sixtyFourKResolved);
    }

    @Test
    void closeRegistrySeriesColorsStayInOneFamily() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture oneKFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xEA, 0xE7, 0xE0),
                GeneratedCellSpriteFixtures.opaque(0xD6, 0xD0, 0xC6)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture twoFiftySixKFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xE6, 0xE2, 0xDA),
                GeneratedCellSpriteFixtures.opaque(0xD1, 0xCB, 0xC0)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture fourMbFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xF0, 0xEC, 0xE4),
                GeneratedCellSpriteFixtures.opaque(0xDB, 0xD5, 0xCB)
        );

        Item oneK = registerSeriesItem("white_shell_cell_1k", oneKFixture);
        Item twoFiftySixK = registerSeriesItem("white_shell_cell_256k", twoFiftySixKFixture);
        Item fourMb = registerSeriesItem("white_shell_cell_4mb", fourMbFixture);

        int oneKResolved = resolveSerialized(new ItemStack(oneK));
        int twoFiftySixKResolved = resolveSerialized(new ItemStack(twoFiftySixK));
        int fourMbResolved = resolveSerialized(new ItemStack(fourMb));

        assertEquals(CellColorMath.postProcessMainColor(oneKFixture.bodyColor), oneKResolved);
        assertEquals(oneKResolved, twoFiftySixKResolved);
        assertEquals(oneKResolved, fourMbResolved);
    }

    @Test
    void distantRegistrySeriesColorsSplitIntoSeparateFamilies() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture grayCanonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0x6F, 0x68, 0x5C),
                GeneratedCellSpriteFixtures.opaque(0x56, 0x50, 0x46)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture grayTierFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0x7A, 0x73, 0x67),
                GeneratedCellSpriteFixtures.opaque(0x60, 0x5A, 0x4F)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture whiteCanonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xFC, 0xFC, 0xF8),
                GeneratedCellSpriteFixtures.opaque(0xF0, 0xEE, 0xE8)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture whiteTierFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xF2, 0xF1, 0xEC),
                GeneratedCellSpriteFixtures.opaque(0xE3, 0xE0, 0xD8)
        );

        Item grayCanonical = registerSeriesItem("mixed_shell_cell_1k", grayCanonicalFixture);
        Item grayTier = registerSeriesItem("mixed_shell_cell_256k", grayTierFixture);
        Item whiteCanonical = registerSeriesItem("mixed_shell_cell_4mb", whiteCanonicalFixture);
        Item whiteTier = registerSeriesItem("mixed_shell_cell_4gb", whiteTierFixture);

        int grayCanonicalResolved = resolveSerialized(new ItemStack(grayCanonical));
        int grayTierResolved = resolveSerialized(new ItemStack(grayTier));
        int whiteCanonicalResolved = resolveSerialized(new ItemStack(whiteCanonical));
        int whiteTierResolved = resolveSerialized(new ItemStack(whiteTier));

        assertEquals(CellColorMath.postProcessMainColor(grayCanonicalFixture.bodyColor), grayCanonicalResolved);
        assertEquals(grayCanonicalResolved, grayTierResolved);
        assertEquals(CellColorMath.postProcessMainColor(whiteCanonicalFixture.bodyColor), whiteCanonicalResolved);
        assertEquals(whiteCanonicalResolved, whiteTierResolved);
        assertNotEquals(grayCanonicalResolved, whiteCanonicalResolved);
    }

    @Test
    void configCanDisableRegistrySeriesFamilySplitting() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture grayCanonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0x6F, 0x68, 0x5C),
                GeneratedCellSpriteFixtures.opaque(0x56, 0x50, 0x46)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture whiteCanonicalFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xFC, 0xFC, 0xF8),
                GeneratedCellSpriteFixtures.opaque(0xF0, 0xEE, 0xE8)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture whiteTierFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xF2, 0xF1, 0xEC),
                GeneratedCellSpriteFixtures.opaque(0xE3, 0xE0, 0xD8)
        );

        Item grayCanonical = registerSeriesItem("mixed_shell_cell_1k", grayCanonicalFixture);
        Item whiteCanonical = registerSeriesItem("mixed_shell_cell_4mb", whiteCanonicalFixture);
        Item whiteTier = registerSeriesItem("mixed_shell_cell_4gb", whiteTierFixture);

        AE2CellRenderConfig.overrideEnableSeriesColorFamiliesForTests(false);
        CellColorResolver.INSTANCE.clear();

        int grayResolved = resolveSerialized(new ItemStack(grayCanonical));
        int whiteCanonicalResolved = resolveSerialized(new ItemStack(whiteCanonical));
        int whiteTierResolved = resolveSerialized(new ItemStack(whiteTier));

        assertEquals(CellColorMath.postProcessMainColor(grayCanonicalFixture.bodyColor), grayResolved);
        assertEquals(grayResolved, whiteCanonicalResolved);
        assertEquals(grayResolved, whiteTierResolved);
    }

    @Test
    void registryFamiliesUseRawMainColorBeforePostProcess() {
        GeneratedCellSpriteFixtures.ModelSpriteFixture oneKFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xF0, 0xA4, 0x9E),
                GeneratedCellSpriteFixtures.opaque(0xAB, 0x5F, 0x75)
        );
        GeneratedCellSpriteFixtures.ModelSpriteFixture oneMbFixture = GeneratedCellSpriteFixtures.modelWithIndicator(
                GeneratedCellSpriteFixtures.opaque(0xF4, 0xC1, 0xBC),
                GeneratedCellSpriteFixtures.opaque(0xFF, 0x75, 0x08)
        );

        Item oneK = registerSeriesItem("crazyae", "energy_cell_1k", oneKFixture);
        Item oneMb = registerSeriesItem("crazyae", "energy_cell_1mb", oneMbFixture);

        int oneKResolved = resolveSerialized(new ItemStack(oneK));
        int oneMbResolved = resolveSerialized(new ItemStack(oneMb));

        assertEquals(CellColorMath.postProcessMainColor(oneKFixture.bodyColor), oneKResolved);
        assertEquals(oneKResolved, oneMbResolved);
    }

    @Test
    void crazyAeStorageCellMegabyteTiersStayWithCanonicalRegistrySeriesColor() {
        Item twoFiftySixK = registerSeriesItemFromResource(
                "crazyae",
                "storage_cell_256k",
                "/com/zzhalex233/ae2cellrender/client/drive/crazyae/storage_cell_256k.png"
        );
        Item oneMb = registerSeriesItemFromResource(
                "crazyae",
                "storage_cell_1mb",
                "/com/zzhalex233/ae2cellrender/client/drive/crazyae/storage_cell_1mb.png"
        );
        Item fourMb = registerSeriesItemFromResource(
                "crazyae",
                "storage_cell_4mb",
                "/com/zzhalex233/ae2cellrender/client/drive/crazyae/storage_cell_4mb.png"
        );
        Item sixtyFourMb = registerSeriesItemFromResource(
                "crazyae",
                "storage_cell_64mb",
                "/com/zzhalex233/ae2cellrender/client/drive/crazyae/storage_cell_64mb.png"
        );

        int canonicalResolved = resolveSerialized(new ItemStack(twoFiftySixK));
        int oneMbResolved = resolveSerialized(new ItemStack(oneMb));
        int fourMbResolved = resolveSerialized(new ItemStack(fourMb));
        int sixtyFourMbResolved = resolveSerialized(new ItemStack(sixtyFourMb));

        assertEquals(canonicalResolved, oneMbResolved);
        assertEquals(canonicalResolved, fourMbResolved);
        assertEquals(canonicalResolved, sixtyFourMbResolved);
    }

    private void register(Item item, GeneratedCellSpriteFixtures.ModelSpriteFixture fixture) {
        ForgeRegistries.ITEMS.register(item);
        Minecraft.getMinecraft().getRenderItem().setModel(item, new FixtureBakedModel(fixture.quads, fixture.bodySprite));
    }

    private Item registerSeriesItem(String path, GeneratedCellSpriteFixtures.ModelSpriteFixture fixture) {
        return registerSeriesItem("aeadditions", path, fixture);
    }

    private Item registerSeriesItem(String domain, String path, GeneratedCellSpriteFixtures.ModelSpriteFixture fixture) {
        Item item = new Item().setRegistryName(new ResourceLocation(domain, path));
        register(item, fixture);
        return item;
    }

    private Item registerSeriesItem(String domain, String path, int bodyColor) {
        Item item = new Item().setRegistryName(new ResourceLocation(domain, path));
        TextureAtlasSprite bodySprite = TextureAtlasSprite.solid("body", 16, 16, bodyColor);
        BakedQuad bodyQuad = new BakedQuad(new int[0], -1, EnumFacing.NORTH, bodySprite);
        ForgeRegistries.ITEMS.register(item);
        Minecraft.getMinecraft().getRenderItem().setModel(item, new FixtureBakedModel(java.util.Collections.singletonList(bodyQuad), bodySprite));
        return item;
    }

    private Item registerSeriesItemFromResource(String domain, String path, String resourcePath) {
        GeneratedCellSpriteFixtures.SpritePixels sprite = GeneratedCellSpriteFixtures.spriteFromResource(resourcePath);
        Item item = new Item().setRegistryName(new ResourceLocation(domain, path));
        TextureAtlasSprite bodySprite = TextureAtlasSprite.fromPixels(path, sprite.width, sprite.height, sprite.pixels);
        BakedQuad bodyQuad = new BakedQuad(new int[0], -1, EnumFacing.NORTH, bodySprite);
        ForgeRegistries.ITEMS.register(item);
        Minecraft.getMinecraft().getRenderItem().setModel(item, new FixtureBakedModel(java.util.Collections.singletonList(bodyQuad), bodySprite));
        return item;
    }

    private void registerTinted(Item item) {
        ForgeRegistries.ITEMS.register(item);
        TextureAtlasSprite sprite = TextureAtlasSprite.solid("tint", 16, 16, 0xFFFFFFFF);
        BakedQuad tintedQuad = new BakedQuad(new int[0], 0, EnumFacing.NORTH, sprite);
        Minecraft.getMinecraft().getRenderItem().setModel(item, new FixtureBakedModel(java.util.Collections.singletonList(tintedQuad), sprite));
    }

    private int resolveSerialized(ItemStack stack) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            ByteBufUtils.writeItemStack(buffer, stack);
            byte[] serialized = new byte[buffer.readableBytes()];
            buffer.getBytes(0, serialized);
            return CellColorResolver.INSTANCE.resolve(serialized);
        } finally {
            buffer.release();
        }
    }

    private static final class FixtureBakedModel implements IBakedModel {
        private final List<BakedQuad> quads;
        private final TextureAtlasSprite particleTexture;

        private FixtureBakedModel(List<BakedQuad> quads, TextureAtlasSprite particleTexture) {
            this.quads = quads;
            this.particleTexture = particleTexture;
        }

        @Override
        public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState state, EnumFacing side, long rand) {
            return quads;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return particleTexture;
        }

        @Override
        public net.minecraft.client.renderer.block.model.ItemCameraTransforms getItemCameraTransforms() {
            return null;
        }

        @Override
        public net.minecraft.client.renderer.block.model.ItemOverrideList getOverrides() {
            return null;
        }
    }

    private static final class MetadataTieredCellItem extends Item {
        private final String registryPath;
        private final int[] metadataValues;

        private MetadataTieredCellItem(int... metadataValues) {
            this("gas_cell", metadataValues);
        }

        private MetadataTieredCellItem(String registryPath, int... metadataValues) {
            this.registryPath = registryPath;
            setRegistryName(new ResourceLocation("aeadditions", registryPath));
            setTranslationKey("com.the9grounds.aeadditions.item.storage.gas.dynamic");
            this.metadataValues = metadataValues;
        }

        @Override
        public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
            for (int metadataValue : metadataValues) {
                items.add(new ItemStack(this, 1, metadataValue));
            }
        }
    }
}
