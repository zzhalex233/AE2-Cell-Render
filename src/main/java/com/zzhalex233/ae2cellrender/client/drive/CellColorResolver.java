package com.zzhalex233.ae2cellrender.client.drive;

import com.zzhalex233.ae2cellrender.config.AE2CellRenderConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CellColorResolver implements IResourceManagerReloadListener {
    private static final float LEGACY_FAMILY_DELTA_E = 18.0F;
    private static final float LEGACY_METADATA_FAMILY_DELTA_E = 24.0F;
    private static final float LEGACY_CHROMATIC_FAMILY_DELTA_E = 42.0F;
    private static final float MEANINGFUL_HUE_SATURATION = 0.18F;
    private static final float NEUTRAL_FAMILY_CHROMA_MAX = 12.0F;

    public static final int NO_COLOR = -1;
    public static final CellColorResolver INSTANCE = new CellColorResolver();

    private final Map<String, Integer> serializedColorCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> seriesColorCache = new ConcurrentHashMap<>();

    private CellColorResolver() {
    }

    public int resolve(byte[] serializedStack) {
        if (serializedStack == null || serializedStack.length == 0) {
            return NO_COLOR;
        }

        String serializedKey = Base64.getEncoder().encodeToString(serializedStack);
        Integer cached = serializedColorCache.get(serializedKey);
        if (cached != null) {
            return cached;
        }

        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            return NO_COLOR;
        }

        ItemStack stack = decode(serializedStack);
        SeriesResolution seriesResolution = resolveSeries(stack);
        String seriesKey = seriesResolution.cacheKey();
        if (seriesKey != null) {
            Integer seriesCached = seriesColorCache.get(seriesKey);
            if (seriesCached != null) {
                serializedColorCache.put(serializedKey, seriesCached);
                return seriesCached;
            }
        }

        int resolved = seriesResolution.resolvedColor();
        if (resolved == NO_COLOR) {
            resolved = resolveDirectColor(seriesResolution.colorSource());
        }
        serializedColorCache.put(serializedKey, resolved);
        if (seriesKey != null) {
            seriesColorCache.put(seriesKey, resolved);
        }
        return resolved;
    }

    public void prime(Iterable<byte[]> serializedStacks) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            throw new IllegalStateException("Color priming must happen on the client thread");
        }

        for (byte[] serializedStack : serializedStacks) {
            resolve(serializedStack);
        }
    }

    public void clear() {
        serializedColorCache.clear();
        seriesColorCache.clear();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        clear();
    }

    private int resolveDirectColor(ItemStack stack) {
        return analyzeDirectColor(stack).finalColor();
    }

    private DirectColorAnalysis analyzeDirectColor(ItemStack stack) {
        if (stack.isEmpty()) {
            return DirectColorAnalysis.none();
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        ItemColors itemColors = minecraft.getItemColors();
        IBakedModel model = minecraft.getRenderItem().getItemModelWithOverrides(stack, null, null);
        if (model == null) {
            return DirectColorAnalysis.none();
        }
        List<BakedQuad> quads = model.getQuads(null, null, 0L);
        int tint = CellColorMath.firstUsableTint(extractTintIndices(quads), tintIndex -> itemColors.colorMultiplier(stack, tintIndex), NO_COLOR);
        if (tint != NO_COLOR) {
            return DirectColorAnalysis.of(tint, tint);
        }

        CellSpriteColorCandidate bestCandidate = null;
        for (TextureAtlasSprite sprite : collectUniqueSprites(quads, model.getParticleTexture())) {
            CellSpriteColorCandidate candidate = resolveSpriteCandidate(sprite);
            if (candidate == null) {
                continue;
            }
            if (bestCandidate == null || isBetterBodyCandidate(candidate, bestCandidate)) {
                bestCandidate = candidate;
            }
        }

        if (bestCandidate == null) {
            return DirectColorAnalysis.none();
        }

        int rawColor = bestCandidate.argb();
        return DirectColorAnalysis.of(rawColor, CellColorMath.postProcessMainColor(rawColor));
    }

    private CellSpriteColorCandidate resolveSpriteCandidate(TextureAtlasSprite sprite) {
        if (sprite == null || sprite.getFrameCount() <= 0) {
            return null;
        }

        int[][] frames = sprite.getFrameTextureData(0);
        if (frames.length == 0 || frames[0].length == 0) {
            return null;
        }

        return CellSpriteColorAnalyzer.mainBodyCandidate(
                frames[0],
                sprite.getIconWidth(),
                sprite.getIconHeight(),
                0xFFFFFFFF
        );
    }

    private int[] extractTintIndices(List<BakedQuad> quads) {
        int tintedQuadCount = 0;
        for (BakedQuad quad : quads) {
            if (quad.hasTintIndex()) {
                tintedQuadCount++;
            }
        }

        if (tintedQuadCount == 0) {
            return new int[0];
        }

        int[] tintIndices = new int[tintedQuadCount];
        int cursor = 0;
        for (BakedQuad quad : quads) {
            if (quad.hasTintIndex()) {
                tintIndices[cursor++] = quad.getTintIndex();
            }
        }
        return tintIndices;
    }

    private Set<TextureAtlasSprite> collectUniqueSprites(List<BakedQuad> quads, TextureAtlasSprite particleTexture) {
        Set<TextureAtlasSprite> sprites = new LinkedHashSet<>();
        for (BakedQuad quad : quads) {
            if (quad.getSprite() != null) {
                sprites.add(quad.getSprite());
            }
        }
        if (particleTexture != null) {
            sprites.add(particleTexture);
        }
        return sprites;
    }

    private boolean isBetterBodyCandidate(CellSpriteColorCandidate left, CellSpriteColorCandidate right) {
        if (left.opaqueSampleCount() != right.opaqueSampleCount()) {
            return left.opaqueSampleCount() > right.opaqueSampleCount();
        }
        if (Float.compare(left.familyWeight(), right.familyWeight()) != 0) {
            return left.familyWeight() > right.familyWeight();
        }
        if (Float.compare(left.clusterWeight(), right.clusterWeight()) != 0) {
            return left.clusterWeight() > right.clusterWeight();
        }
        if (Float.compare(left.innerWeightRatio(), right.innerWeightRatio()) != 0) {
            return left.innerWeightRatio() > right.innerWeightRatio();
        }
        if (left.clusterCount() != right.clusterCount()) {
            return left.clusterCount() < right.clusterCount();
        }
        return Float.compare(left.averageLightness(), right.averageLightness()) > 0;
    }

    private SeriesResolution resolveSeries(ItemStack stack) {
        if (stack.isEmpty()) {
            return SeriesResolution.direct(ItemStack.EMPTY);
        }

        SeriesResolution registrySeries = resolveRegistrySeries(stack);
        if (registrySeries != null) {
            return registrySeries;
        }

        SeriesResolution metadataFamilySeries = resolveMetadataFamilySeries(stack);
        if (metadataFamilySeries != null) {
            return metadataFamilySeries;
        }

        return SeriesResolution.direct(stack);
    }

    private SeriesResolution resolveRegistrySeries(ItemStack stack) {
        ResourceLocation registryName = stack.getItem().getRegistryName();
        CellSeriesKey key = CellSeriesKey.from(registryName);
        if (!key.isGrouped()) {
            return null;
        }

        RegistrySeriesFamily family = null;
        if (AE2CellRenderConfig.isSeriesColorFamiliesEnabled()) {
            family = resolveRegistrySeriesFamily(stack, key);
        }

        net.minecraft.item.Item canonicalItem = CellSeriesRegistryLookup.findCanonicalItem(stack.getItem());
        boolean splitFamily = false;
        String familyKey = null;
        int resolvedColor = NO_COLOR;
        if (family != null && family.canonicalItem() != null) {
            canonicalItem = family.canonicalItem();
            splitFamily = family.splitFamily();
            familyKey = family.familyKey();
            resolvedColor = family.canonicalColor();
        }

        ItemStack colorSource = stack;
        if (canonicalItem != null && canonicalItem != stack.getItem()) {
            colorSource = new ItemStack(
                    canonicalItem,
                    stack.getCount(),
                    stack.getMetadata(),
                    copyTag(stack.getTagCompound())
            );
        }

        String cacheKey = key.domain() + ":" + key.normalizedPath();
        if (splitFamily && familyKey != null && !familyKey.isEmpty()) {
            cacheKey += ":family=" + familyKey;
        }

        return new SeriesResolution(
                cacheKey + ":m=" + stack.getMetadata() + ":t=" + tagSignature(stack),
                colorSource,
                resolvedColor
        );
    }

    private SeriesResolution resolveMetadataFamilySeries(ItemStack stack) {
        if (!isMetadataFamilyCandidate(stack)) {
            return null;
        }

        MetadataFamily family = resolveMetadataFamily(stack);
        if (family == null) {
            return null;
        }

        ResourceLocation registryName = stack.getItem().getRegistryName();
        ItemStack colorSource = new ItemStack(
                stack.getItem(),
                stack.getCount(),
                family.canonicalMetadata(),
                copyTag(stack.getTagCompound())
        );
        return new SeriesResolution(
                registryName.getNamespace() + ":" + registryName.getPath() + ":family=" + family.canonicalMetadata() + ":t=" + tagSignature(stack),
                colorSource,
                NO_COLOR
        );
    }

    private boolean isMetadataFamilyCandidate(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == null || stack.getItem().getRegistryName() == null) {
            return false;
        }

        Set<Integer> metadataValues = new LinkedHashSet<>();
        for (ItemStack candidate : enumerateVariants(stack)) {
            if (candidate.isEmpty() || candidate.getItem() != stack.getItem()) {
                continue;
            }
            metadataValues.add(candidate.getMetadata());
            if (metadataValues.size() > 1) {
                return true;
            }
        }
        return false;
    }

    private MetadataFamily resolveMetadataFamily(ItemStack stack) {
        List<MetadataVariantColor> variants = collectMetadataVariantColors(stack);
        if (variants.size() < 2) {
            return null;
        }

        if (!AE2CellRenderConfig.isSeriesColorFamiliesEnabled()) {
            // Family splitting is off, so one series stays on a single canonical metadata.
            return new MetadataFamily(findLowestMetadata(variants));
        }

        int targetMetadata = stack.getMetadata();
        int[] parents = buildFamilyForest(variants, metadataFamilyThreshold());

        Map<Integer, MetadataFamilyAccumulator> groupedFamilies = new LinkedHashMap<>();
        for (int index = 0; index < variants.size(); index++) {
            MetadataVariantColor variant = variants.get(index);
            int root = find(parents, index);
            MetadataFamilyAccumulator accumulator = groupedFamilies.get(root);
            if (accumulator == null) {
                accumulator = new MetadataFamilyAccumulator();
                groupedFamilies.put(root, accumulator);
            }
            accumulator.include(variant);
        }

        for (MetadataFamilyAccumulator accumulator : groupedFamilies.values()) {
            if (accumulator.contains(targetMetadata)) {
                return accumulator.toFamily();
            }
        }

        return null;
    }

    private List<MetadataVariantColor> collectMetadataVariantColors(ItemStack stack) {
        Map<Integer, ItemStack> distinctVariants = new LinkedHashMap<>();
        for (ItemStack candidate : enumerateVariants(stack)) {
            if (candidate.isEmpty() || candidate.getItem() != stack.getItem()) {
                continue;
            }
            distinctVariants.putIfAbsent(candidate.getMetadata(), candidate.copy());
        }

        List<MetadataVariantColor> variants = new ArrayList<>();
        for (ItemStack candidate : distinctVariants.values()) {
            DirectColorAnalysis analysis = analyzeDirectColor(candidate);
            if (analysis.finalColor() == NO_COLOR) {
                continue;
            }
            variants.add(new MetadataVariantColor(
                    candidate.getMetadata(),
                    analysis.finalColor(),
                    CellColorMath.lab(analysis.rawColor()),
                    CellColorMath.hsv(analysis.rawColor())
            ));
        }
        return variants;
    }

    private RegistrySeriesFamily resolveRegistrySeriesFamily(ItemStack stack, CellSeriesKey currentKey) {
        List<RegistrySeriesVariantColor> variants = collectRegistrySeriesVariantColors(stack, currentKey);
        if (variants.isEmpty()) {
            return null;
        }

        int[] parents = buildFamilyForest(variants);
        Map<Integer, RegistrySeriesFamilyAccumulator> groupedFamilies = new LinkedHashMap<>();
        Integer targetRoot = null;
        for (int index = 0; index < variants.size(); index++) {
            RegistrySeriesVariantColor variant = variants.get(index);
            int root = find(parents, index);
            RegistrySeriesFamilyAccumulator accumulator = groupedFamilies.get(root);
            if (accumulator == null) {
                accumulator = new RegistrySeriesFamilyAccumulator();
                groupedFamilies.put(root, accumulator);
            }
            accumulator.include(variant);
            if (variant.item() == stack.getItem()) {
                targetRoot = root;
            }
        }

        if (targetRoot == null) {
            return null;
        }

        RegistrySeriesFamilyAccumulator targetFamily = groupedFamilies.get(targetRoot);
        return targetFamily == null ? null : targetFamily.toFamily(groupedFamilies.size() > 1);
    }

    private List<RegistrySeriesVariantColor> collectRegistrySeriesVariantColors(ItemStack stack, CellSeriesKey currentKey) {
        Map<String, RegistryCandidate> candidates = new LinkedHashMap<>();
        for (net.minecraft.item.Item candidate : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (candidate == null || candidate.getRegistryName() == null) {
                continue;
            }

            ResourceLocation candidateName = candidate.getRegistryName();
            CellSeriesKey candidateKey = CellSeriesKey.from(candidateName);
            if (!candidateKey.isGrouped()) {
                continue;
            }
            if (!currentKey.domain().equals(candidateKey.domain())) {
                continue;
            }
            if (!currentKey.normalizedPath().equals(candidateKey.normalizedPath())) {
                continue;
            }

            candidates.put(candidateName.toString(), new RegistryCandidate(candidate, candidateName, candidateKey));
        }

        if (candidates.size() < 2) {
            return new ArrayList<>();
        }

        Map<String, RegistrySeriesVariantColor> variants = new LinkedHashMap<>();
        for (RegistryCandidate candidate : candidates.values()) {
            ItemStack candidateStack = new ItemStack(
                    candidate.item(),
                    stack.getCount(),
                    stack.getMetadata(),
                    copyTag(stack.getTagCompound())
            );
            DirectColorAnalysis analysis = analyzeDirectColor(candidateStack);
            if (analysis.finalColor() == NO_COLOR) {
                continue;
            }
            variants.put(candidate.registryName().toString(), new RegistrySeriesVariantColor(
                    candidate.item(),
                    candidate.registryName(),
                    candidate.seriesKey(),
                    analysis.finalColor(),
                    CellColorMath.lab(analysis.rawColor()),
                    CellColorMath.hsv(analysis.rawColor())
            ));
        }
        return new ArrayList<>(variants.values());
    }

    private <T extends FamilyColorVariant> int[] buildFamilyForest(List<T> variants) {
        return buildFamilyForest(variants, generalFamilyThreshold());
    }

    private <T extends FamilyColorVariant> int[] buildFamilyForest(List<T> variants, float deltaEThreshold) {
        int[] parents = new int[variants.size()];
        for (int index = 0; index < parents.length; index++) {
            parents[index] = index;
        }

        for (int leftIndex = 0; leftIndex < variants.size(); leftIndex++) {
            for (int rightIndex = leftIndex + 1; rightIndex < variants.size(); rightIndex++) {
                if (sameColorFamily(variants.get(leftIndex), variants.get(rightIndex), deltaEThreshold)) {
                    union(parents, leftIndex, rightIndex);
                }
            }
        }
        return parents;
    }

    private boolean sameColorFamily(FamilyColorVariant left, FamilyColorVariant right) {
        return sameColorFamily(left, right, generalFamilyThreshold());
    }

    private boolean sameColorFamily(FamilyColorVariant left, FamilyColorVariant right, float deltaEThreshold) {
        if (isNearNeutral(left.lab()) && isNearNeutral(right.lab())) {
            float neutralThreshold = AE2CellRenderConfig.familyNeutralLightnessThreshold();
            if (CellColorMath.deltaE(left.lab(), right.lab()) > deltaEThreshold) {
                return false;
            }
            // Gray shells care more about brightness layering than tiny hue drift.
            return Math.abs(left.lab().lightness() - right.lab().lightness()) <= neutralThreshold;
        }

        if (AE2CellRenderConfig.isPreferSameHueFamiliesEnabled() && hasMeaningfulHue(left.hsv()) && hasMeaningfulHue(right.hsv())) {
            float hueThreshold = AE2CellRenderConfig.familyHueThreshold();
            if (CellColorMath.hueDistance(left.hsv().hue(), right.hsv().hue()) > hueThreshold) {
                return false;
            }
            return CellColorMath.deltaE(left.lab(), right.lab()) <= chromaticFamilyThreshold();
        }

        // Once hue stays in range, the remaining split is just overall color drift.
        return CellColorMath.deltaE(left.lab(), right.lab()) <= deltaEThreshold;
    }

    private float generalFamilyThreshold() {
        return scaledFamilyThreshold(LEGACY_FAMILY_DELTA_E);
    }

    private float metadataFamilyThreshold() {
        return scaledFamilyThreshold(LEGACY_METADATA_FAMILY_DELTA_E);
    }

    private float chromaticFamilyThreshold() {
        return AE2CellRenderConfig.familyColorDistanceThreshold();
    }

    private float scaledFamilyThreshold(float legacyThreshold) {
        float configuredThreshold = chromaticFamilyThreshold();
        if (configuredThreshold <= 0.0F) {
            return 0.0F;
        }
        return configuredThreshold * (legacyThreshold / LEGACY_CHROMATIC_FAMILY_DELTA_E);
    }

    private boolean isNearNeutral(CellColorMath.LabColor color) {
        float chroma = (float) Math.sqrt((color.a() * color.a()) + (color.b() * color.b()));
        return chroma <= NEUTRAL_FAMILY_CHROMA_MAX;
    }

    private boolean hasMeaningfulHue(CellColorMath.HsvColor color) {
        return color.saturation() >= MEANINGFUL_HUE_SATURATION;
    }

    private int find(int[] parents, int index) {
        int parent = parents[index];
        if (parent == index) {
            return index;
        }
        parents[index] = find(parents, parent);
        return parents[index];
    }

    private int findLowestMetadata(List<MetadataVariantColor> variants) {
        int canonicalMetadata = Integer.MAX_VALUE;
        for (MetadataVariantColor variant : variants) {
            if (variant.metadata() < canonicalMetadata) {
                canonicalMetadata = variant.metadata();
            }
        }
        return canonicalMetadata;
    }

    private void union(int[] parents, int leftIndex, int rightIndex) {
        int leftRoot = find(parents, leftIndex);
        int rightRoot = find(parents, rightIndex);
        if (leftRoot == rightRoot) {
            return;
        }
        if (leftRoot < rightRoot) {
            parents[rightRoot] = leftRoot;
        } else {
            parents[leftRoot] = rightRoot;
        }
    }

    private List<ItemStack> enumerateVariants(ItemStack stack) {
        List<ItemStack> variants = new ArrayList<>();
        variants.add(stack);

        NonNullList<ItemStack> subItems = NonNullList.create();
        stack.getItem().getSubItems(CreativeTabs.SEARCH, subItems);
        for (ItemStack candidate : subItems) {
            if (candidate != null && !candidate.isEmpty()) {
                variants.add(candidate);
            }
        }
        return variants;
    }

    private String tagSignature(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        return tagCompound == null ? "" : tagCompound.toString();
    }

    private NBTTagCompound copyTag(NBTTagCompound tagCompound) {
        return tagCompound == null ? null : tagCompound.copy();
    }

    private ItemStack decode(byte[] serializedStack) {
        ByteBuf buffer = Unpooled.wrappedBuffer(serializedStack);
        try {
            ItemStack stack = ByteBufUtils.readItemStack(buffer);
            return stack == null ? ItemStack.EMPTY : stack;
        } catch (RuntimeException exception) {
            return ItemStack.EMPTY;
        } finally {
            buffer.release();
        }
    }

    private static final class SeriesResolution {
        private final String cacheKey;
        private final ItemStack colorSource;
        private final int resolvedColor;

        private SeriesResolution(String cacheKey, ItemStack colorSource, int resolvedColor) {
            this.cacheKey = cacheKey;
            this.colorSource = colorSource;
            this.resolvedColor = resolvedColor;
        }

        private static SeriesResolution direct(ItemStack stack) {
            return new SeriesResolution(null, stack, NO_COLOR);
        }

        private String cacheKey() {
            return cacheKey;
        }

        private ItemStack colorSource() {
            return colorSource;
        }

        private int resolvedColor() {
            return resolvedColor;
        }
    }

    private static final class DirectColorAnalysis {
        private final int rawColor;
        private final int finalColor;

        private DirectColorAnalysis(int rawColor, int finalColor) {
            this.rawColor = rawColor;
            this.finalColor = finalColor;
        }

        private static DirectColorAnalysis none() {
            return new DirectColorAnalysis(NO_COLOR, NO_COLOR);
        }

        private static DirectColorAnalysis of(int rawColor, int finalColor) {
            return new DirectColorAnalysis(rawColor, finalColor);
        }

        private int rawColor() {
            return rawColor;
        }

        private int finalColor() {
            return finalColor;
        }
    }

    private static final class MetadataVariantColor implements FamilyColorVariant {
        private final int metadata;
        private final int color;
        private final CellColorMath.LabColor lab;
        private final CellColorMath.HsvColor hsv;

        private MetadataVariantColor(int metadata, int color, CellColorMath.LabColor lab, CellColorMath.HsvColor hsv) {
            this.metadata = metadata;
            this.color = color;
            this.lab = lab;
            this.hsv = hsv;
        }

        private int metadata() {
            return metadata;
        }

        private int color() {
            return color;
        }

        @Override
        public CellColorMath.LabColor lab() {
            return lab;
        }

        @Override
        public CellColorMath.HsvColor hsv() {
            return hsv;
        }
    }

    private interface FamilyColorVariant {
        CellColorMath.LabColor lab();

        CellColorMath.HsvColor hsv();
    }

    private static final class RegistrySeriesVariantColor implements FamilyColorVariant {
        private final net.minecraft.item.Item item;
        private final ResourceLocation registryName;
        private final CellSeriesKey seriesKey;
        private final int color;
        private final CellColorMath.LabColor lab;
        private final CellColorMath.HsvColor hsv;

        private RegistrySeriesVariantColor(
                net.minecraft.item.Item item,
                ResourceLocation registryName,
                CellSeriesKey seriesKey,
                int color,
                CellColorMath.LabColor lab,
                CellColorMath.HsvColor hsv
        ) {
            this.item = item;
            this.registryName = registryName;
            this.seriesKey = seriesKey;
            this.color = color;
            this.lab = lab;
            this.hsv = hsv;
        }

        private net.minecraft.item.Item item() {
            return item;
        }

        private ResourceLocation registryName() {
            return registryName;
        }

        private CellSeriesKey seriesKey() {
            return seriesKey;
        }

        private int color() {
            return color;
        }

        @Override
        public CellColorMath.LabColor lab() {
            return lab;
        }

        @Override
        public CellColorMath.HsvColor hsv() {
            return hsv;
        }
    }

    private static final class RegistryCandidate {
        private final net.minecraft.item.Item item;
        private final ResourceLocation registryName;
        private final CellSeriesKey seriesKey;

        private RegistryCandidate(net.minecraft.item.Item item, ResourceLocation registryName, CellSeriesKey seriesKey) {
            this.item = item;
            this.registryName = registryName;
            this.seriesKey = seriesKey;
        }

        private net.minecraft.item.Item item() {
            return item;
        }

        private ResourceLocation registryName() {
            return registryName;
        }

        private CellSeriesKey seriesKey() {
            return seriesKey;
        }
    }

    private static final class MetadataFamilyAccumulator {
        private int canonicalMetadata = Integer.MAX_VALUE;
        private final Set<Integer> members = new LinkedHashSet<>();

        private void include(MetadataVariantColor variant) {
            members.add(variant.metadata());
            if (variant.metadata() < canonicalMetadata) {
                canonicalMetadata = variant.metadata();
            }
        }

        private boolean contains(int metadata) {
            return members.contains(metadata);
        }

        private MetadataFamily toFamily() {
            return members.isEmpty() ? null : new MetadataFamily(canonicalMetadata);
        }
    }

    private static final class RegistrySeriesFamilyAccumulator {
        private RegistrySeriesVariantColor canonical;

        private void include(RegistrySeriesVariantColor variant) {
            if (canonical == null || isBetterCanonical(variant, canonical)) {
                canonical = variant;
            }
        }

        private boolean isBetterCanonical(RegistrySeriesVariantColor candidate, RegistrySeriesVariantColor current) {
            int capacityComparison = candidate.seriesKey().capacityKey().compareTo(current.seriesKey().capacityKey());
            if (capacityComparison != 0) {
                return capacityComparison < 0;
            }
            return candidate.registryName().toString().compareTo(current.registryName().toString()) < 0;
        }

        private RegistrySeriesFamily toFamily(boolean splitFamily) {
            return canonical == null ? null : new RegistrySeriesFamily(canonical.item(), canonical.registryName().toString(), splitFamily, canonical.color());
        }
    }

    private static final class MetadataFamily {
        private final int canonicalMetadata;

        private MetadataFamily(int canonicalMetadata) {
            this.canonicalMetadata = canonicalMetadata;
        }

        private int canonicalMetadata() {
            return canonicalMetadata;
        }
    }

    private static final class RegistrySeriesFamily {
        private final net.minecraft.item.Item canonicalItem;
        private final String familyKey;
        private final boolean splitFamily;
        private final int canonicalColor;

        private RegistrySeriesFamily(net.minecraft.item.Item canonicalItem, String familyKey, boolean splitFamily, int canonicalColor) {
            this.canonicalItem = canonicalItem;
            this.familyKey = familyKey;
            this.splitFamily = splitFamily;
            this.canonicalColor = canonicalColor;
        }

        private net.minecraft.item.Item canonicalItem() {
            return canonicalItem;
        }

        private String familyKey() {
            return familyKey;
        }

        private boolean splitFamily() {
            return splitFamily;
        }

        private int canonicalColor() {
            return canonicalColor;
        }
    }
}
