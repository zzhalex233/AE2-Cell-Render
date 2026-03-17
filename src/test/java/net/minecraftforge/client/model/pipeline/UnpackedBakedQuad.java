package net.minecraftforge.client.model.pipeline;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

public class UnpackedBakedQuad extends BakedQuad {

    public static class Builder implements IVertexConsumer {
        public Builder(VertexFormat format) {
        }

        public void setApplyDiffuseLighting(boolean applyDiffuseLighting) {
        }

        public void setTexture(TextureAtlasSprite sprite) {
        }

        public void setQuadOrientation(EnumFacing facing) {
        }

        public UnpackedBakedQuad build() {
            return new UnpackedBakedQuad();
        }
    }
}
