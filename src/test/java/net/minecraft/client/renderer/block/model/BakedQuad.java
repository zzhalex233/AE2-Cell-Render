package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

public class BakedQuad {
    private TextureAtlasSprite sprite;
    private boolean hasTintIndex;
    private int tintIndex;

    public BakedQuad() {
    }

    public BakedQuad(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite) {
        this.sprite = sprite;
        this.tintIndex = tintIndex;
        this.hasTintIndex = true;
    }

    public BakedQuad(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite, boolean applyDiffuseLighting, VertexFormat format) {
        this(vertexData, tintIndex, face, sprite);
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public int[] getVertexData() {
        return new int[0];
    }

    public boolean hasTintIndex() {
        return hasTintIndex;
    }

    public int getTintIndex() {
        return tintIndex;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
        this.hasTintIndex = true;
    }

    public void setHasTintIndex(boolean hasTintIndex) {
        this.hasTintIndex = hasTintIndex;
    }

    public EnumFacing getFace() {
        return EnumFacing.NORTH;
    }

    public void pipe(net.minecraftforge.client.model.pipeline.IVertexConsumer consumer) {
    }

    public VertexFormat getFormat() {
        return null;
    }

    public boolean shouldApplyDiffuseLighting() {
        return false;
    }
}
