package com.zzhalex233.ae2cellrender.mixin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AE2CellRenderAccessTransformerTest {

    @Test
    void accessTransformerConfigBridgesKnownRuntimeFieldsForCompatibilityTestMods() throws IOException {
        InputStream stream = AE2CellRenderAccessTransformerTest.class.getClassLoader()
                .getResourceAsStream("ae2cellrender_at.cfg");
        assertNotNull(stream, "Expected ae2cellrender_at.cfg on the classpath");
        try {
            String config = readFully(stream);
            assertTrue(config.contains("net.minecraft.item.ItemArmor ARMOR_MODIFIERS"));
            assertTrue(config.contains("net.minecraft.client.renderer.BufferBuilder isDrawing"));
            assertTrue(config.contains("net.minecraft.server.management.PlayerList commandsAllowedForAll"));
            assertTrue(config.contains("net.minecraft.inventory.Container listeners"));
        } finally {
            stream.close();
        }
    }

    private static String readFully(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
