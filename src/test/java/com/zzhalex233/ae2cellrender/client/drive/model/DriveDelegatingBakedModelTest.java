package com.zzhalex233.ae2cellrender.client.drive.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix4f;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveDelegatingBakedModelTest {

    @Test
    void ignoresExtendedStateThatDoesNotExposeVisualProperty() {
        List<BakedQuad> baseQuads = Collections.singletonList(null);
        DriveDelegatingBakedModel model = new DriveDelegatingBakedModel(new DummyModel(baseQuads));
        IExtendedBlockState state = new MissingVisualState();

        List<BakedQuad> quads = assertDoesNotThrow(() -> model.getQuads(state, null, 0L));

        assertSame(baseQuads, quads);
    }

    @Test
    void emitsTheSameRgbForEveryCellBodyFace() throws Exception {
        DriveDelegatingBakedModel model = new DriveDelegatingBakedModel(new DummyModel(Collections.emptyList()));
        DriveCellBodyModelBuilder.DriveCellBodyModel bodyModel = new DriveCellBodyModelBuilder().create(0);
        TextureAtlasSprite sprite = TextureAtlasSprite.solid("overlay", 16, 16, 0xFFFFFFFF);
        List<BakedQuad> quads = new java.util.ArrayList<>();
        Matrix4f transform = new Matrix4f();
        transform.setIdentity();
        Method appendModelQuads = DriveDelegatingBakedModel.class.getDeclaredMethod(
                "appendModelQuads",
                List.class,
                TextureAtlasSprite.class,
                Matrix4f.class,
                DriveCellBodyModelBuilder.DriveCellBodyModel.class,
                int.class
        );
        appendModelQuads.setAccessible(true);

        appendModelQuads.invoke(model, quads, sprite, transform, bodyModel, 0x80A1B2C3);

        assertEquals(5, quads.size());
        int expectedRgb = quads.get(0).getVertexData()[0];
        for (int i = 0; i < quads.size(); i++) {
            int[] vertexData = quads.get(i).getVertexData();
            assertTrue(vertexData.length > 0, "quad " + i + " did not retain emitted vertex data");
            assertEquals(expectedRgb, vertexData[0], "quad " + i + " emitted a different RGB");
        }
    }

    @Test
    void cellBodyQuadsDoNotApplyDiffuseLighting() throws Exception {
        DriveDelegatingBakedModel model = new DriveDelegatingBakedModel(new DummyModel(Collections.emptyList()));
        DriveCellBodyModelBuilder.DriveCellBodyModel bodyModel = new DriveCellBodyModelBuilder().create(0);
        TextureAtlasSprite sprite = TextureAtlasSprite.solid("overlay", 16, 16, 0xFFFFFFFF);
        List<BakedQuad> quads = new java.util.ArrayList<>();
        Matrix4f transform = new Matrix4f();
        transform.setIdentity();
        Method appendModelQuads = DriveDelegatingBakedModel.class.getDeclaredMethod(
                "appendModelQuads",
                List.class,
                TextureAtlasSprite.class,
                Matrix4f.class,
                DriveCellBodyModelBuilder.DriveCellBodyModel.class,
                int.class
        );
        appendModelQuads.setAccessible(true);

        appendModelQuads.invoke(model, quads, sprite, transform, bodyModel, 0xFF8899AA);

        assertEquals(5, quads.size());
        for (int i = 0; i < quads.size(); i++) {
            assertTrue(!quads.get(i).shouldApplyDiffuseLighting(), "quad " + i + " still applies diffuse lighting");
        }
    }

    @Test
    void cellBodyQuadsKeepResolvedColorWhenFlatLit() throws Exception {
        DriveDelegatingBakedModel model = new DriveDelegatingBakedModel(new DummyModel(Collections.emptyList()));
        DriveCellBodyModelBuilder.DriveCellBodyModel bodyModel = new DriveCellBodyModelBuilder().create(0);
        TextureAtlasSprite sprite = TextureAtlasSprite.solid("overlay", 16, 16, 0xFFFFFFFF);
        List<BakedQuad> quads = new java.util.ArrayList<>();
        Matrix4f transform = new Matrix4f();
        transform.setIdentity();
        Method appendModelQuads = DriveDelegatingBakedModel.class.getDeclaredMethod(
                "appendModelQuads",
                List.class,
                TextureAtlasSprite.class,
                Matrix4f.class,
                DriveCellBodyModelBuilder.DriveCellBodyModel.class,
                int.class
        );
        appendModelQuads.setAccessible(true);

        appendModelQuads.invoke(model, quads, sprite, transform, bodyModel, 0xFFA1B2C3);

        assertEquals(5, quads.size());
        int emittedRgb = quads.get(0).getVertexData()[0];
        assertEquals(0x00A1B2C3, emittedRgb, "flat-lit quads should keep the resolved display color");
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
        public ItemCameraTransforms getItemCameraTransforms() {
            return null;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return null;
        }
    }
}
