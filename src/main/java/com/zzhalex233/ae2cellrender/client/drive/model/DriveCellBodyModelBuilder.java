package com.zzhalex233.ae2cellrender.client.drive.model;

import com.zzhalex233.ae2cellrender.drive.DriveCellSlotLayout;
import com.zzhalex233.ae2cellrender.drive.DriveSlotLayouts;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DriveCellBodyModelBuilder {

    static final float FRONT_OVERDRAW = 0.03F / 16.0F;
    static final float BACK_OVERDRAW = 0.1F / 16.0F;
    static final float SIDE_OVERDRAW = 0.03F / 16.0F;

    public DriveCellBodyModel create(int slot) {
        return create(slot, null);
    }

    public DriveCellBodyModel create(int slot, @Nullable String layoutId) {
        DriveSlotLayouts.Layout layout = DriveSlotLayouts.forId(layoutId);
        DriveCellSlotLayout.SlotRect rect = layout.slot(slot).renderedRect();
        DriveCellSlotLayout.SlotRect expandedRect = new DriveCellSlotLayout.SlotRect(
                rect.minX() - SIDE_OVERDRAW,
                rect.minY(),
                rect.maxX() + SIDE_OVERDRAW,
                rect.maxY()
        );
        float frontZ = layout.frontZ() - FRONT_OVERDRAW;
        float backZ = layout.backZ() + BACK_OVERDRAW;

        Face front = Face.front(expandedRect, frontZ, DriveMaterialRegions.frontRegion());
        Face top = Face.extruded(FaceKind.TOP, expandedRect.minX(), expandedRect.maxY(), expandedRect.maxX(), expandedRect.maxY(), frontZ, backZ, DriveMaterialRegions.topRegion());
        Face left = Face.extruded(FaceKind.LEFT, expandedRect.minX(), expandedRect.minY(), expandedRect.minX(), expandedRect.maxY(), frontZ, backZ, DriveMaterialRegions.sideRegion());
        Face right = Face.extruded(FaceKind.RIGHT, expandedRect.maxX(), expandedRect.minY(), expandedRect.maxX(), expandedRect.maxY(), frontZ, backZ, DriveMaterialRegions.sideRegion());
        Face bottom = Face.extruded(FaceKind.BOTTOM, expandedRect.minX(), expandedRect.minY(), expandedRect.maxX(), expandedRect.minY(), frontZ, backZ, DriveMaterialRegions.bottomRegion());

        return new DriveCellBodyModel(expandedRect, frontZ, backZ, Arrays.asList(front, top, left, right, bottom));
    }

    public static final class DriveCellBodyModel {
        private final DriveCellSlotLayout.SlotRect frontRect;
        private final float frontZ;
        private final float backZ;
        private final List<Face> faces;

        DriveCellBodyModel(DriveCellSlotLayout.SlotRect frontRect, float frontZ, float backZ, List<Face> faces) {
            this.frontRect = frontRect;
            this.frontZ = frontZ;
            this.backZ = backZ;
            this.faces = Collections.unmodifiableList(faces);
        }

        public DriveCellSlotLayout.SlotRect getFrontRect() {
            return frontRect;
        }

        public float frontZ() {
            return frontZ;
        }

        public float backZ() {
            return backZ;
        }

        public List<Face> getFaces() {
            return faces;
        }
    }

    public enum FaceKind {
        FRONT,
        TOP,
        LEFT,
        RIGHT,
        BOTTOM
    }

    public static final class Face {
        private final FaceKind kind;
        private final float minX;
        private final float minY;
        private final float maxX;
        private final float maxY;
        private final float nearZ;
        private final float farZ;
        private final DriveMaterialRegions.MaterialRegion material;

        private Face(FaceKind kind, float minX, float minY, float maxX, float maxY, float nearZ, float farZ, DriveMaterialRegions.MaterialRegion material) {
            this.kind = kind;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.nearZ = nearZ;
            this.farZ = farZ;
            this.material = material;
        }

        private static Face front(DriveCellSlotLayout.SlotRect rect, float frontZ, DriveMaterialRegions.MaterialRegion material) {
            return new Face(FaceKind.FRONT, rect.minX(), rect.minY(), rect.maxX(), rect.maxY(), frontZ, frontZ, material);
        }

        private static Face extruded(FaceKind kind, float minX, float minY, float maxX, float maxY, float nearZ, float farZ, DriveMaterialRegions.MaterialRegion material) {
            return new Face(kind, minX, minY, maxX, maxY, nearZ, farZ, material);
        }

        public FaceKind getKind() {
            return kind;
        }

        public float getMinX() {
            return minX;
        }

        public float getMinY() {
            return minY;
        }

        public float getMaxX() {
            return maxX;
        }

        public float getMaxY() {
            return maxY;
        }

        public float getNearZ() {
            return nearZ;
        }

        public float getFarZ() {
            return farZ;
        }

        public DriveMaterialRegions.MaterialRegion getMaterial() {
            return material;
        }
    }
}
