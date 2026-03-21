package net.minecraft.client.renderer.texture;

import java.util.ArrayList;
import java.util.List;

public class TextureAtlasSprite {
    private String iconName = "test";
    private int iconWidth = 16;
    private int iconHeight = 16;
    private List<int[][]> framesTextureData = new ArrayList<>();

    public static TextureAtlasSprite solid(String iconName, int width, int height, int argb) {
        TextureAtlasSprite sprite = new TextureAtlasSprite();
        sprite.iconName = iconName;
        sprite.iconWidth = width;
        sprite.iconHeight = height;
        int[] frame = new int[width * height];
        for (int i = 0; i < frame.length; i++) {
            frame[i] = argb;
        }
        sprite.framesTextureData = new ArrayList<>();
        sprite.framesTextureData.add(new int[][]{frame});
        return sprite;
    }

    public static TextureAtlasSprite fromPixels(String iconName, int width, int height, int[] pixels) {
        if (pixels.length != width * height) {
            throw new IllegalArgumentException("Pixel data does not match width/height");
        }
        TextureAtlasSprite sprite = new TextureAtlasSprite();
        sprite.iconName = iconName;
        sprite.iconWidth = width;
        sprite.iconHeight = height;
        sprite.framesTextureData = new ArrayList<>();
        sprite.framesTextureData.add(new int[][]{pixels.clone()});
        return sprite;
    }

    public void initSprite(int originX, int originY, int width, int height, boolean rotated) {
    }

    public void copyFrom(TextureAtlasSprite sprite) {
        this.iconName = sprite.iconName;
        this.iconWidth = sprite.iconWidth;
        this.iconHeight = sprite.iconHeight;
        this.framesTextureData = new ArrayList<>(sprite.framesTextureData);
    }

    public int getOriginX() {
        return 0;
    }

    public int getOriginY() {
        return 0;
    }

    public int getIconWidth() {
        return iconWidth;
    }

    public int getIconHeight() {
        return iconHeight;
    }

    public float getMinU() {
        return 0.0F;
    }

    public float getMaxU() {
        return 1.0F;
    }

    public float getInterpolatedU(double value) {
        return (float) (value / iconWidth);
    }

    public float getUnInterpolatedU(float value) {
        return value * iconWidth;
    }

    public float getMinV() {
        return 0.0F;
    }

    public float getMaxV() {
        return 1.0F;
    }

    public float getInterpolatedV(double value) {
        return (float) (value / iconHeight);
    }

    public float getUnInterpolatedV(float value) {
        return value * iconHeight;
    }

    public String getIconName() {
        return iconName;
    }

    public void updateAnimation() {
    }

    public int[][] getFrameTextureData(int frameIndex) {
        return framesTextureData.isEmpty() ? new int[0][] : framesTextureData.get(Math.min(frameIndex, framesTextureData.size() - 1));
    }

    public int getFrameCount() {
        return framesTextureData.size();
    }

    public void setIconWidth(int width) {
        this.iconWidth = width;
    }

    public void setIconHeight(int height) {
        this.iconHeight = height;
    }

    public void clearFramesTextureData() {
        this.framesTextureData = new ArrayList<>();
    }

    public boolean hasAnimationMetadata() {
        return false;
    }

    public void setFramesTextureData(List<int[][]> framesTextureData) {
        this.framesTextureData = new ArrayList<>(framesTextureData);
    }

    public String toString() {
        return iconName;
    }
}
