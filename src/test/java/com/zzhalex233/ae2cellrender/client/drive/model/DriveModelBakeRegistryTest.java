package com.zzhalex233.ae2cellrender.client.drive.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.TextureAtlasSprite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveModelBakeRegistryTest {

    @Test
    void wrapsRegisteredModel() {
        DriveModelBakeRegistry.reset();
        ModelResourceLocation target = new ModelResourceLocation("test", "drive");
        DriveModelBakeRegistry.register(target);
        IBakedModel original = new DummyModel();

        IBakedModel wrapped = DriveModelBakeRegistry.wrap(target, original);

        assertNotSame(original, wrapped);
        assertTrue(wrapped instanceof DriveDelegatingBakedModel);
    }

    @Test
    void leavesUnknownModelsUntouched() {
        DriveModelBakeRegistry.reset();
        ModelResourceLocation other = new ModelResourceLocation("test", "other");
        IBakedModel original = new DummyModel();

        IBakedModel wrapped = DriveModelBakeRegistry.wrap(other, original);

        assertSame(original, wrapped);
    }

    private static final class DummyModel implements IBakedModel {
        @Override
        public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(net.minecraft.block.state.IBlockState state, net.minecraft.util.EnumFacing side, long rand) {
            return java.util.Collections.emptyList();
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
