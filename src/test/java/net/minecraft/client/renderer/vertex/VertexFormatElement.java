package net.minecraft.client.renderer.vertex;

public class VertexFormatElement {

    public enum EnumUsage {
        POSITION,
        COLOR,
        UV,
        NORMAL,
        PADDING,
        GENERIC,
        MATRIX
    }

    public EnumUsage getUsage() {
        return EnumUsage.GENERIC;
    }

    public int getIndex() {
        return 0;
    }
}
