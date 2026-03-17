package com.zzhalex233.ae2cellrender.client.drive.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

class DriveDelegatingBakedModelTest {

    @Test
    void ignoresExtendedStateThatDoesNotExposeVisualProperty() {
        List<BakedQuad> baseQuads = Collections.singletonList(null);
        DriveDelegatingBakedModel model = new DriveDelegatingBakedModel(new DummyModel(baseQuads));
        IExtendedBlockState state = new MissingVisualState();

        List<BakedQuad> quads = assertDoesNotThrow(() -> model.getQuads(state, null, 0L));

        assertSame(baseQuads, quads);
    }

    private static final class MissingVisualState implements IExtendedBlockState {

        @Override
        public Collection<IUnlistedProperty<?>> getUnlistedNames() {
            return Collections.emptyList();
        }

        @Override
        public <T> T getValue(IUnlistedProperty<T> property) {
            throw new IllegalArgumentException("missing property: " + property.getName());
        }
    }

    private static final class DummyModel implements IBakedModel {
        private final List<BakedQuad> quads;

        private DummyModel(List<BakedQuad> quads) {
            this.quads = quads;
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
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
            return null;
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
