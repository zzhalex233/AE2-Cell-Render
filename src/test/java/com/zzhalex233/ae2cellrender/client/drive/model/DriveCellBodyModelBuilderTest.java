package com.zzhalex233.ae2cellrender.client.drive.model;

import com.zzhalex233.ae2cellrender.drive.DriveCellSlotLayout;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveCellBodyModelBuilderTest {

    @Test
    void builderUsesRenderedSlotFootprintAndDepth() {
        DriveCellBodyModelBuilder builder = new DriveCellBodyModelBuilder();
        DriveCellBodyModelBuilder.DriveCellBodyModel model = builder.create(2);

        DriveCellSlotLayout.SlotRect expected = DriveCellSlotLayout.getRendered(2);
        assertEquals(expected.minX() - DriveCellBodyModelBuilder.SIDE_OVERDRAW, model.getFrontRect().minX(), 1.0e-6F);
        assertEquals(expected.minY(), model.getFrontRect().minY(), 1.0e-6F);
        assertEquals(expected.maxX() + DriveCellBodyModelBuilder.SIDE_OVERDRAW, model.getFrontRect().maxX(), 1.0e-6F);
        assertEquals(expected.maxY(), model.getFrontRect().maxY(), 1.0e-6F);
        assertEquals(DriveCellSlotLayout.FRONT_Z - DriveCellBodyModelBuilder.FRONT_OVERDRAW, model.frontZ(), 1.0e-6F);
        assertEquals(DriveCellSlotLayout.BACK_Z + DriveCellBodyModelBuilder.BACK_OVERDRAW, model.backZ(), 1.0e-6F);
        assertTrue(model.frontZ() < DriveCellSlotLayout.FRONT_Z);
        assertTrue(model.backZ() > DriveCellSlotLayout.BACK_Z);
        assertTrue(model.getFrontRect().minX() < expected.minX());
        assertTrue(model.getFrontRect().maxX() > expected.maxX());
    }

    @Test
    void builderEmitsFiveFacesAndFrontUsesCutoutMaterial() {
        DriveCellBodyModelBuilder builder = new DriveCellBodyModelBuilder();
        DriveCellBodyModelBuilder.DriveCellBodyModel model = builder.create(0);

        EnumSet<DriveCellBodyModelBuilder.FaceKind> kinds = EnumSet.noneOf(DriveCellBodyModelBuilder.FaceKind.class);
        for (DriveCellBodyModelBuilder.Face face : model.getFaces()) {
            kinds.add(face.getKind());
        }

        assertEquals(5, model.getFaces().size());
        assertEquals(EnumSet.allOf(DriveCellBodyModelBuilder.FaceKind.class), kinds);
        assertTrue(model.getFaces().get(0).getMaterial().usesCutoutMask());
    }
}
