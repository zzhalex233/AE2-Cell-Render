package com.zzhalex233.ae2cellrender.client.drive;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CellColorResolver implements IResourceManagerReloadListener {

    public static final int NO_COLOR = -1;
    public static final CellColorResolver INSTANCE = new CellColorResolver();

    private final Map<String, Integer> colorCache = new ConcurrentHashMap<>();

    private CellColorResolver() {
    }

    public int resolve(byte[] serializedStack) {
        if (serializedStack == null || serializedStack.length == 0) {
            return NO_COLOR;
        }

        String key = Base64.getEncoder().encodeToString(serializedStack);
        Integer cached = colorCache.get(key);
        if (cached != null) {
            return cached;
        }

        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            return NO_COLOR;
        }

        int resolved = resolve(decode(serializedStack));
        colorCache.put(key, resolved);
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
        colorCache.clear();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        clear();
        primeCachedSnapshots();
    }

    private void primeCachedSnapshots() {
        for (com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot snapshot : DriveRenderCache.getInstance().getSnapshots()) {
            prime(snapshot.getSlots());
        }
    }

    private int resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return NO_COLOR;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        ItemColors itemColors = minecraft.getItemColors();
        IBakedModel model = minecraft.getRenderItem().getItemModelWithOverrides(stack, null, null);
        List<BakedQuad> quads = model.getQuads(null, null, 0L);
        int tint = CellColorMath.firstUsableTint(extractTintIndices(quads), tintIndex -> itemColors.colorMultiplier(stack, tintIndex), NO_COLOR);
        if (tint != NO_COLOR) {
            return tint;
        }

        for (BakedQuad quad : quads) {
            int color = resolveSprite(quad.getSprite());
            if (color != NO_COLOR) {
                return color;
            }
        }

        return resolveSprite(model.getParticleTexture());
    }

    private int resolveSprite(TextureAtlasSprite sprite) {
        if (sprite == null || sprite.getFrameCount() <= 0) {
            return NO_COLOR;
        }

        int[][] frames = sprite.getFrameTextureData(0);
        if (frames.length == 0 || frames[0].length == 0) {
            return NO_COLOR;
        }

        return CellSpriteColorAnalyzer.mainBodyColor(
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
}
