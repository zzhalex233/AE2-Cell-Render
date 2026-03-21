package com.zzhalex233.ae2cellrender.client.drive;

import com.zzhalex233.ae2cellrender.drive.DriveRenderSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DriveClientEventHandlerReloadTest {

    @AfterEach
    void resetMinecraft() {
        Minecraft.getMinecraft().reset();
        DriveRenderCache.getInstance().clear();
        CellColorResolver.INSTANCE.clear();
    }

    @Test
    void resourceReloadMarksCachedDriveInCurrentDimensionForRebuild() {
        WorldClient world = new WorldClient();
        world.provider.setDimension(7);
        Minecraft.getMinecraft().world = world;

        DriveRenderCache.getInstance().store(new DriveRenderSnapshot(7, 12345L, Collections.singletonList(new byte[] {1})), 11);
        DriveRenderCache.getInstance().store(new DriveRenderSnapshot(8, 22222L, Collections.singletonList(new byte[] {2})), 12);

        new DriveClientEventHandler(DriveRenderCache.getInstance()).onResourceManagerReload(null);

        assertEquals(Collections.singletonList("12345->12345"), world.getRenderUpdates());
    }
}