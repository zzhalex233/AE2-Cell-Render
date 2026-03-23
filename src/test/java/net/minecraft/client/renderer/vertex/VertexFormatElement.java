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

    private final EnumUsage usage;
    private final int index;

    public VertexFormatElement() {
        this(EnumUsage.GENERIC, 0);
    }

    public VertexFormatElement(EnumUsage usage, int index) {
        this.usage = usage;
        this.index = index;
    }

    public EnumUsage getUsage() {
        return usage;
    }

    public int getIndex() {
        return index;
    }
}