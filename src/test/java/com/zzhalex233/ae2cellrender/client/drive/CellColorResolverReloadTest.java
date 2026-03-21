package com.zzhalex233.ae2cellrender.client.drive;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CellColorResolverReloadTest {

    @AfterEach
    void resetMinecraft() {
        Minecraft.getMinecraft().reset();
        ForgeRegistries.ITEMS.clear();
        DriveRenderCache.getInstance().clear();
        CellColorResolver.INSTANCE.clear();
    }

    @Test
    void resourceReloadClearsCachesWithoutPrimingCachedSnapshots() {
        ForgeRegistries.ITEMS.register(Items.FIERY_STORAGE_CELL_1K);

        AtomicInteger quadCalls = new AtomicInteger();
        TextureAtlasSprite sprite = TextureAtlasSprite.solid("body", 16, 16, 0xFFCC8844);
        Minecraft.getMinecraft().getRenderItem().setModel(Items.FIERY_STORAGE_CELL_1K, new CountingBakedModel(quadCalls, sprite));

        byte[] serialized = serialized(new ItemStack(Items.FIERY_STORAGE_CELL_1K));
        CellColorResolver.INSTANCE.resolve(serialized);
        assertEquals(1, quadCalls.get());

        DriveRenderCache.getInstance().store(new com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot(0, 99L, Collections.singletonList(serialized)), 5);

        CellColorResolver.INSTANCE.onResourceManagerReload(null);

        assertEquals(1, quadCalls.get());
    }

    private byte[] serialized(ItemStack stack) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            ByteBufUtils.writeItemStack(buffer, stack);
            byte[] serialized = new byte[buffer.readableBytes()];
            buffer.getBytes(0, serialized);
            return serialized;
        } finally {
            buffer.release();
        }
    }

    private static final class CountingBakedModel implements IBakedModel {
        private final AtomicInteger quadCalls;
        private final TextureAtlasSprite sprite;

        private CountingBakedModel(AtomicInteger quadCalls, TextureAtlasSprite sprite) {
            this.quadCalls = quadCalls;
            this.sprite = sprite;
        }

        @Override
        public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState state, EnumFacing side, long rand) {
            quadCalls.incrementAndGet();
            return Collections.singletonList(new BakedQuad(new int[0], -1, EnumFacing.NORTH, sprite));
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
            return sprite;
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
}