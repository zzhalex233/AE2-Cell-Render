package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

public class BakedQuad {
    private final int[] vertexData;
    private TextureAtlasSprite sprite;
    private boolean hasTintIndex;
    private int tintIndex;
    private final EnumFacing face;
    private final boolean diffuseLighting;

    public BakedQuad() {
        this(new int[0], -1, EnumFacing.NORTH, null, false, null);
    }

    public BakedQuad(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite) {
        this(vertexData, tintIndex, face, sprite, false, null);
    }

    public BakedQuad(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite, boolean applyDiffuseLighting, VertexFormat format) {
        this.vertexData = vertexData == null ? new int[0] : vertexData.clone();
        this.sprite = sprite;
        this.tintIndex = tintIndex;
        this.hasTintIndex = tintIndex >= 0;
        this.face = face == null ? EnumFacing.NORTH : face;
        this.diffuseLighting = applyDiffuseLighting;
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public int[] getVertexData() {
        return vertexData.clone();
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
        return face;
    }

    public void pipe(net.minecraftforge.client.model.pipeline.IVertexConsumer consumer) {
    }

    public VertexFormat getFormat() {
        return null;
    }

    public boolean shouldApplyDiffuseLighting() {
        return diffuseLighting;
    }
}