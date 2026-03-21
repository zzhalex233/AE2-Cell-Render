package com.zzhalex233.ae2cellrender.client.drive.model;

import appeng.client.render.FacingToRotation;
import com.zzhalex233.ae2cellrender.client.drive.CellColorResolver;
import com.zzhalex233.ae2cellrender.client.drive.DriveClientEventHandler;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveSlotVisual;
import com.zzhalex233.ae2cellrender.client.drive.compat.DriveVisualState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DriveDelegatingBakedModel implements IBakedModel {

    private static final DriveCellBodyModelBuilder BODY_MODEL_BUILDER = new DriveCellBodyModelBuilder();
    private static final float TOP_MULTIPLIER = 1.05F;
    private static final float SIDE_MULTIPLIER = 0.93F;
    private static final float BOTTOM_MULTIPLIER = 0.92F;

    private final IBakedModel delegate;

    public DriveDelegatingBakedModel(IBakedModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> baseQuads = delegate.getQuads(state, side, rand);
        if (side != null) {
            return baseQuads;
        }

        DriveVisualState visualState = readVisualState(state);
        if (visualState == null || !visualState.isOnline() || visualState.getSlots().isEmpty()) {
            return baseQuads;
        }

        List<BakedQuad> quads = new ArrayList<>(baseQuads);
        appendDriveCellBodyQuads(quads, visualState, rand);
        return Collections.unmodifiableList(quads);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return delegate.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return delegate.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return delegate.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return delegate.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return delegate.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return delegate.getOverrides();
    }

    @Nullable
    private DriveVisualState readVisualState(@Nullable IBlockState state) {
        if (!(state instanceof IExtendedBlockState)) {
            return null;
        }

        IExtendedBlockState extendedState = (IExtendedBlockState) state;
        if (!extendedState.getUnlistedNames().contains(DriveVisualProperty.INSTANCE)) {
            return null;
        }

        try {
            return extendedState.getValue(DriveVisualProperty.INSTANCE);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void appendDriveCellBodyQuads(List<BakedQuad> quads, DriveVisualState visualState, long rand) {
        TextureAtlasSprite sprite = DriveClientEventHandler.getOverlaySprite();
        if (sprite == null) {
            return;
        }

        FacingToRotation rotation = FacingToRotation.get(visualState.getForward(), visualState.getUp());
        Matrix4f transform = rotation.getMat();

        for (DriveSlotVisual slot : visualState.getSlots()) {
            byte[] serializedStack = slot.getSerializedStack();
            if (serializedStack.length == 0) {
                continue;
            }

            int color = CellColorResolver.INSTANCE.resolve(serializedStack);
            if (color == CellColorResolver.NO_COLOR) {
                continue;
            }

            DriveCellBodyModelBuilder.DriveCellBodyModel model = BODY_MODEL_BUILDER.create(slot.getSlotIndex(), slot.getLayoutId());
            appendModelQuads(quads, sprite, transform, model, color);
        }
    }

    private void appendModelQuads(List<BakedQuad> quads, TextureAtlasSprite sprite, Matrix4f transform, DriveCellBodyModelBuilder.DriveCellBodyModel model, int color) {
        int alpha = (color >>> 24) & 0xFF;
        int frontColor = color & 0x00FFFFFF;
        int topColor = shadeColor(color, TOP_MULTIPLIER);
        int sideColor = shadeColor(color, SIDE_MULTIPLIER);
        int bottomColor = shadeColor(color, BOTTOM_MULTIPLIER);

        for (DriveCellBodyModelBuilder.Face face : model.getFaces()) {
            int faceRgb = faceColor(face.getKind(), frontColor, topColor, sideColor, bottomColor);
            quads.add(buildQuad(sprite, transform, face, faceRgb, alpha));
        }
    }

    private BakedQuad buildQuad(TextureAtlasSprite sprite, Matrix4f transform, DriveCellBodyModelBuilder.Face face, int rgb, int alpha) {
        Vector3f[] vertices = buildOrderedVertices(face, transform);
        float minU = interpolatedU(sprite, face.getMaterial().getMinU());
        float maxU = interpolatedU(sprite, face.getMaterial().getMaxU());
        float minV = interpolatedV(sprite, face.getMaterial().getMinV());
        float maxV = interpolatedV(sprite, face.getMaterial().getMaxV());
        EnumFacing facing = facingFor(face.getKind(), transform);

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
        builder.setApplyDiffuseLighting(true);
        builder.setTexture(sprite);
        builder.setQuadOrientation(facing);

        putVertex(builder, facing, vertices[0], minU, minV, rgb, alpha);
        putVertex(builder, facing, vertices[1], minU, maxV, rgb, alpha);
        putVertex(builder, facing, vertices[2], maxU, maxV, rgb, alpha);
        putVertex(builder, facing, vertices[3], maxU, minV, rgb, alpha);
        return builder.build();
    }

    private Vector3f[] buildOrderedVertices(DriveCellBodyModelBuilder.Face face, Matrix4f transform) {
        Vector3f[] vertices = buildVertices(face, transform);
        if (face.getKind() != DriveCellBodyModelBuilder.FaceKind.FRONT) {
            return vertices;
        }

        return new Vector3f[] {
                vertices[3],
                vertices[2],
                vertices[1],
                vertices[0]
        };
    }

    private Vector3f[] buildVertices(DriveCellBodyModelBuilder.Face face, Matrix4f transform) {
        switch (face.getKind()) {
            case FRONT:
                return new Vector3f[] {
                        rotate(transform, face.getMinX(), face.getMaxY(), face.getNearZ()),
                        rotate(transform, face.getMinX(), face.getMinY(), face.getNearZ()),
                        rotate(transform, face.getMaxX(), face.getMinY(), face.getNearZ()),
                        rotate(transform, face.getMaxX(), face.getMaxY(), face.getNearZ())
                };
            case TOP:
                return new Vector3f[] {
                        rotate(transform, face.getMinX(), face.getMinY(), face.getFarZ()),
                        rotate(transform, face.getMaxX(), face.getMaxY(), face.getFarZ()),
                        rotate(transform, face.getMaxX(), face.getMaxY(), face.getNearZ()),
                        rotate(transform, face.getMinX(), face.getMinY(), face.getNearZ())
                };
            case LEFT:
                return new Vector3f[] {
                        rotate(transform, face.getMinX(), face.getMaxY(), face.getFarZ()),
                        rotate(transform, face.getMaxX(), face.getMinY(), face.getNearZ()),
                        rotate(transform, face.getMaxX(), face.getMinY(), face.getFarZ()),
                        rotate(transform, face.getMinX(), face.getMaxY(), face.getNearZ())
                };
            case RIGHT:
                return new Vector3f[] {
                        rotate(transform, face.getMinX(), face.getMaxY(), face.getNearZ()),
                        rotate(transform, face.getMaxX(), face.getMinY(), face.getFarZ()),
                        rotate(transform, face.getMaxX(), face.getMinY(), face.getNearZ()),
                        rotate(transform, face.getMinX(), face.getMaxY(), face.getFarZ())
                };
            case BOTTOM:
                return new Vector3f[] {
                        rotate(transform, face.getMinX(), face.getMaxY(), face.getNearZ()),
                        rotate(transform, face.getMaxX(), face.getMinY(), face.getNearZ()),
                        rotate(transform, face.getMaxX(), face.getMinY(), face.getFarZ()),
                        rotate(transform, face.getMinX(), face.getMaxY(), face.getFarZ())
                };
            default:
                throw new IllegalStateException("Unhandled face kind: " + face.getKind());
        }
    }

    private EnumFacing facingFor(DriveCellBodyModelBuilder.FaceKind kind, Matrix4f transform) {
        Vector3f normal;
        switch (kind) {
            case FRONT:
                normal = new Vector3f(0.0F, 0.0F, -1.0F);
                break;
            case TOP:
                normal = new Vector3f(0.0F, 1.0F, 0.0F);
                break;
            case LEFT:
                normal = new Vector3f(-1.0F, 0.0F, 0.0F);
                break;
            case RIGHT:
                normal = new Vector3f(1.0F, 0.0F, 0.0F);
                break;
            case BOTTOM:
                normal = new Vector3f(0.0F, -1.0F, 0.0F);
                break;
            default:
                throw new IllegalStateException("Unhandled face kind: " + kind);
        }
        transform.transform(normal);
        return EnumFacing.getFacingFromVector(normal.x, normal.y, normal.z);
    }

    private int faceColor(DriveCellBodyModelBuilder.FaceKind kind, int frontColor, int topColor, int sideColor, int bottomColor) {
        switch (kind) {
            case FRONT:
                return frontColor;
            case TOP:
                return topColor;
            case LEFT:
            case RIGHT:
                return sideColor;
            case BOTTOM:
                return bottomColor;
            default:
                throw new IllegalStateException("Unhandled face kind: " + kind);
        }
    }

    private int shadeColor(int argb, float multiplier) {
        return (shadeComponent((argb >>> 16) & 0xFF, multiplier) << 16)
                | (shadeComponent((argb >>> 8) & 0xFF, multiplier) << 8)
                | shadeComponent(argb & 0xFF, multiplier);
    }

    private int shadeComponent(int component, float multiplier) {
        int shaded = Math.round(component * multiplier);
        if (shaded < 0) {
            return 0;
        }
        return Math.min(255, shaded);
    }

    private void putVertex(IVertexConsumer consumer, EnumFacing facing, Vector3f vertex, float u, float v, int rgb, int alpha) {
        for (int element = 0; element < DefaultVertexFormats.BLOCK.getElementCount(); element++) {
            switch (DefaultVertexFormats.BLOCK.getElement(element).getUsage()) {
                case POSITION:
                    consumer.put(element, vertex.x, vertex.y, vertex.z, 1.0F);
                    break;
                case COLOR:
                    consumer.put(
                            element,
                            ((rgb >>> 16) & 0xFF) / 255.0F,
                            ((rgb >>> 8) & 0xFF) / 255.0F,
                            (rgb & 0xFF) / 255.0F,
                            alpha / 255.0F
                    );
                    break;
                case UV:
                    if (DefaultVertexFormats.BLOCK.getElement(element).getIndex() == 0) {
                        consumer.put(element, u, v, 0.0F, 1.0F);
                    } else {
                        consumer.put(element);
                    }
                    break;
                case NORMAL:
                    consumer.put(element, normalX(facing), normalY(facing), normalZ(facing), 0.0F);
                    break;
                default:
                    consumer.put(element);
                    break;
            }
        }
    }

    private float interpolatedU(TextureAtlasSprite sprite, float normalizedU) {
        return sprite.getInterpolatedU(normalizedU * 16.0F);
    }

    private float interpolatedV(TextureAtlasSprite sprite, float normalizedV) {
        return sprite.getInterpolatedV(normalizedV * 16.0F);
    }

    private float normalX(EnumFacing facing) {
        switch (facing) {
            case EAST:
                return 1.0F;
            case WEST:
                return -1.0F;
            default:
                return 0.0F;
        }
    }

    private float normalY(EnumFacing facing) {
        switch (facing) {
            case UP:
                return 1.0F;
            case DOWN:
                return -1.0F;
            default:
                return 0.0F;
        }
    }

    private float normalZ(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                return 1.0F;
            case NORTH:
                return -1.0F;
            default:
                return 0.0F;
        }
    }

    private Vector3f rotate(Matrix4f transform, float x, float y, float z) {
        Vector3f vertex = new Vector3f(x - 0.5F, y - 0.5F, z - 0.5F);
        transform.transform(vertex);
        vertex.x += 0.5F;
        vertex.y += 0.5F;
        vertex.z += 0.5F;
        return vertex;
    }
}
