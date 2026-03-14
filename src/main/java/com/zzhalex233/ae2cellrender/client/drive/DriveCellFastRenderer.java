package com.zzhalex233.ae2cellrender.client.drive;

import appeng.client.render.FacingToRotation;
import appeng.tile.storage.TileDrive;
import com.zzhalex233.ae2cellrender.drive.DriveCellSlotLayout;
import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import com.zzhalex233.ae2cellrender.network.AE2CellRenderNetwork;
import com.zzhalex233.ae2cellrender.network.message.RequestDriveRenderDataMessage;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.animation.FastTESR;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public final class DriveCellFastRenderer extends FastTESR<TileDrive> {

    private static final float OVERLAY_MIN_U = 0.0F;
    private static final float OVERLAY_MAX_U = 6.0F;
    private static final float OVERLAY_MIN_V = 0.0F;
    private static final float OVERLAY_MAX_V = 2.0F;
    private static final float SOLID_SAMPLE_U = 8.0F;
    private static final float SOLID_SAMPLE_V = 8.0F;
    private static final float SHELL_FRONT_OVERLAP = 0.03F / 16.0F;
    private static final float SHELL_BACK_EXTRA = 0.035F / 16.0F;
    private static final float TOP_MULTIPLIER = 1.05F;
    private static final float SIDE_MULTIPLIER = 0.93F;
    private static final float BOTTOM_MULTIPLIER = 0.86F;

    private final DriveRenderCache cache;

    public DriveCellFastRenderer(DriveRenderCache cache) {
        this.cache = cache;
    }

    public static int buildStateDigest(TileDrive drive) {
        int digest = 1;
        for (int slot = 0; slot < DriveCellSlotLayout.SLOT_COUNT; slot++) {
            digest = 31 * digest + drive.getCellStatus(slot);
        }
        return digest;
    }

    @Override
    public void renderTileEntityFast(TileDrive drive, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        if (drive.getWorld() == null || destroyStage >= 0) {
            return;
        }
        if (!DriveCellRenderGate.shouldRenderColorLayer(drive.isPowered())) {
            return;
        }

        buffer.setTranslation(x, y, z);
        try {
            int dimensionId = drive.getWorld().provider.getDimension();
            long positionKey = drive.getPos().toLong();
            int digest = buildStateDigest(drive);
            DriveRenderSnapshot snapshot = cache.getSnapshot(dimensionId, positionKey, digest);
            if (snapshot == null) {
                requestSnapshot(drive, dimensionId, positionKey, digest);
                return;
            }

            TextureAtlasSprite sprite = DriveClientEventHandler.getOverlaySprite();
            if (sprite == null) {
                return;
            }

            FacingToRotation rotation = FacingToRotation.get(drive.getForward(), drive.getUp());
            Matrix4f transform = rotation.getMat();
            int packedLight = drive.getWorld().getCombinedLight(drive.getPos().offset(drive.getForward()), 0);
            int lightmapU = (packedLight >> 16) & 0xFFFF;
            int lightmapV = packedLight & 0xFFFF;

            for (int slot = 0; slot < DriveCellSlotLayout.SLOT_COUNT; slot++) {
                byte[] serializedStack = snapshot.getSlots().get(slot);
                if (serializedStack.length == 0) {
                    continue;
                }

                int color = CellColorResolver.INSTANCE.resolve(serializedStack);
                if (color == CellColorResolver.NO_COLOR) {
                    continue;
                }

                renderSlot(buffer, transform, sprite, slot, color, lightmapU, lightmapV);
            }
        } finally {
            // TileEntityRendererDispatcher calls renderTileEntityFast directly during batch rendering,
            // so the FastTESR wrapper does not always reset translation for us.
            buffer.setTranslation(0.0D, 0.0D, 0.0D);
        }
    }

    private void requestSnapshot(TileDrive drive, int dimensionId, long positionKey, int digest) {
        long worldTime = drive.getWorld().getTotalWorldTime();
        if (cache.shouldRequest(dimensionId, positionKey, digest, worldTime)) {
            AE2CellRenderNetwork.CHANNEL.sendToServer(new RequestDriveRenderDataMessage(positionKey));
        }
    }

    private void renderSlot(BufferBuilder buffer, Matrix4f transform, TextureAtlasSprite sprite, int slot, int color, int lightmapU, int lightmapV) {
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        DriveCellSlotLayout.SlotRect rect = DriveCellSlotLayout.getRendered(slot);
        DriveCellSlotLayout.SlotRect fullRect = DriveCellSlotLayout.get(slot);

        renderFrontFace(buffer, transform, sprite, fullRect, rect, red, green, blue, alpha, lightmapU, lightmapV);
        renderExtrudedShell(buffer, transform, sprite, rect, red, green, blue, alpha, lightmapU, lightmapV);
    }

    private void renderFrontFace(BufferBuilder buffer, Matrix4f transform, TextureAtlasSprite sprite, DriveCellSlotLayout.SlotRect fullRect, DriveCellSlotLayout.SlotRect rect, int red, int green, int blue, int alpha, int lightmapU, int lightmapV) {
        Vector3f topLeft = rotate(transform, rect.minX(), rect.maxY(), DriveCellSlotLayout.FRONT_Z);
        Vector3f bottomLeft = rotate(transform, rect.minX(), rect.minY(), DriveCellSlotLayout.FRONT_Z);
        Vector3f bottomRight = rotate(transform, rect.maxX(), rect.minY(), DriveCellSlotLayout.FRONT_Z);
        Vector3f topRight = rotate(transform, rect.maxX(), rect.maxY(), DriveCellSlotLayout.FRONT_Z);
        float minU = sprite.getInterpolatedU(OVERLAY_MIN_U + overlayPixelsForX(fullRect, rect.minX() - fullRect.minX()));
        float maxU = sprite.getInterpolatedU(OVERLAY_MAX_U - overlayPixelsForX(fullRect, fullRect.maxX() - rect.maxX()));
        float minV = sprite.getInterpolatedV(OVERLAY_MIN_V + overlayPixelsForY(fullRect, fullRect.maxY() - rect.maxY()));
        float maxV = sprite.getInterpolatedV(OVERLAY_MAX_V);

        putVertex(buffer, topLeft, minU, minV, red, green, blue, alpha, lightmapU, lightmapV);
        putVertex(buffer, bottomLeft, minU, maxV, red, green, blue, alpha, lightmapU, lightmapV);
        putVertex(buffer, bottomRight, maxU, maxV, red, green, blue, alpha, lightmapU, lightmapV);
        putVertex(buffer, topRight, maxU, minV, red, green, blue, alpha, lightmapU, lightmapV);
    }

    private void renderExtrudedShell(BufferBuilder buffer, Matrix4f transform, TextureAtlasSprite sprite, DriveCellSlotLayout.SlotRect rect, int red, int green, int blue, int alpha, int lightmapU, int lightmapV) {
        float solidU = sprite.getInterpolatedU(SOLID_SAMPLE_U);
        float solidV = sprite.getInterpolatedV(SOLID_SAMPLE_V);
        float shellFrontZ = DriveCellSlotLayout.FRONT_Z - SHELL_FRONT_OVERLAP;
        float shellBackZ = DriveCellSlotLayout.BACK_Z + SHELL_FRONT_OVERLAP + SHELL_BACK_EXTRA;
        Vector3f topFrontLeft = rotate(transform, rect.minX(), rect.maxY(), shellFrontZ);
        Vector3f topFrontRight = rotate(transform, rect.maxX(), rect.maxY(), shellFrontZ);
        Vector3f topBackLeft = rotate(transform, rect.minX(), rect.maxY(), shellBackZ);
        Vector3f topBackRight = rotate(transform, rect.maxX(), rect.maxY(), shellBackZ);
        Vector3f bottomFrontLeft = rotate(transform, rect.minX(), rect.minY(), shellFrontZ);
        Vector3f bottomFrontRight = rotate(transform, rect.maxX(), rect.minY(), shellFrontZ);
        Vector3f bottomBackLeft = rotate(transform, rect.minX(), rect.minY(), shellBackZ);
        Vector3f bottomBackRight = rotate(transform, rect.maxX(), rect.minY(), shellBackZ);

        int topColor = DriveCellShading.blendColor(red, green, blue, TOP_MULTIPLIER);
        int sideColor = DriveCellShading.blendColor(red, green, blue, SIDE_MULTIPLIER);
        int bottomColor = DriveCellShading.blendColor(red, green, blue, BOTTOM_MULTIPLIER);

        renderColoredQuad(buffer, topBackLeft, topBackRight, topFrontRight, topFrontLeft, solidU, solidV, topColor, alpha, lightmapU, lightmapV);
        renderColoredQuad(buffer, bottomFrontLeft, bottomFrontRight, bottomBackRight, bottomBackLeft, solidU, solidV, bottomColor, alpha, lightmapU, lightmapV);
        renderColoredQuad(buffer, topBackLeft, topFrontLeft, bottomFrontLeft, bottomBackLeft, solidU, solidV, sideColor, alpha, lightmapU, lightmapV);
        renderColoredQuad(buffer, topFrontRight, topBackRight, bottomBackRight, bottomFrontRight, solidU, solidV, sideColor, alpha, lightmapU, lightmapV);
    }

    private void renderColoredQuad(BufferBuilder buffer, Vector3f vertex0, Vector3f vertex1, Vector3f vertex2, Vector3f vertex3, float u, float v, int rgb, int alpha, int lightmapU, int lightmapV) {
        int red = (rgb >>> 16) & 0xFF;
        int green = (rgb >>> 8) & 0xFF;
        int blue = rgb & 0xFF;
        putVertex(buffer, vertex0, u, v, red, green, blue, alpha, lightmapU, lightmapV);
        putVertex(buffer, vertex1, u, v, red, green, blue, alpha, lightmapU, lightmapV);
        putVertex(buffer, vertex2, u, v, red, green, blue, alpha, lightmapU, lightmapV);
        putVertex(buffer, vertex3, u, v, red, green, blue, alpha, lightmapU, lightmapV);
    }

    private float overlayPixelsForX(DriveCellSlotLayout.SlotRect fullRect, float inset) {
        float width = fullRect.maxX() - fullRect.minX();
        return (inset / width) * (OVERLAY_MAX_U - OVERLAY_MIN_U);
    }

    private float overlayPixelsForY(DriveCellSlotLayout.SlotRect fullRect, float inset) {
        float height = fullRect.maxY() - fullRect.minY();
        return (inset / height) * (OVERLAY_MAX_V - OVERLAY_MIN_V);
    }

    private void putVertex(BufferBuilder buffer, Vector3f vertex, float u, float v, int red, int green, int blue, int alpha, int lightmapU, int lightmapV) {
        buffer.pos(vertex.x, vertex.y, vertex.z)
                .color(red, green, blue, alpha)
                .tex(u, v)
                .lightmap(lightmapU, lightmapV)
                .endVertex();
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
