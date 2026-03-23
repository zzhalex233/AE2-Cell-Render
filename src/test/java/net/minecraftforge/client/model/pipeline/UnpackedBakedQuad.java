package net.minecraftforge.client.model.pipeline;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;

public class UnpackedBakedQuad extends BakedQuad {

    public UnpackedBakedQuad() {
        this(new int[0], -1, EnumFacing.NORTH, null, false, null);
    }

    public UnpackedBakedQuad(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite, boolean applyDiffuseLighting, VertexFormat format) {
        super(vertexData, tintIndex, face, sprite, applyDiffuseLighting, format);
    }

    public static class Builder implements IVertexConsumer {
        private int rgb;
        private int alpha;
        private TextureAtlasSprite sprite;
        private EnumFacing facing = EnumFacing.NORTH;
        private boolean applyDiffuseLighting;

        public Builder(VertexFormat format) {
        }

        public void setApplyDiffuseLighting(boolean applyDiffuseLighting) {
            this.applyDiffuseLighting = applyDiffuseLighting;
        }

        public void setTexture(TextureAtlasSprite sprite) {
            this.sprite = sprite;
        }

        public void setQuadOrientation(EnumFacing facing) {
            this.facing = facing;
        }

        @Override
        public void put(int element, float... values) {
            VertexFormatElement.EnumUsage usage = DefaultVertexFormats.BLOCK.getElement(element).getUsage();
            if (usage == VertexFormatElement.EnumUsage.COLOR) {
                int red = Math.round(values[0] * 255.0F) & 0xFF;
                int green = Math.round(values[1] * 255.0F) & 0xFF;
                int blue = Math.round(values[2] * 255.0F) & 0xFF;
                rgb = (red << 16) | (green << 8) | blue;
                alpha = values.length > 3 ? Math.round(values[3] * 255.0F) & 0xFF : 0xFF;
            }
        }

        public UnpackedBakedQuad build() {
            return new UnpackedBakedQuad(new int[] { rgb, alpha }, -1, facing, sprite, applyDiffuseLighting, null);
        }
    }
}