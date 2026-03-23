package net.minecraft.client.renderer.vertex;

public class VertexFormat {

    private final VertexFormatElement[] elements = new VertexFormatElement[] {
            new VertexFormatElement(VertexFormatElement.EnumUsage.POSITION, 0),
            new VertexFormatElement(VertexFormatElement.EnumUsage.COLOR, 0),
            new VertexFormatElement(VertexFormatElement.EnumUsage.UV, 0),
            new VertexFormatElement(VertexFormatElement.EnumUsage.NORMAL, 0)
    };

    public int getElementCount() {
        return elements.length;
    }

    public VertexFormatElement getElement(int index) {
        return elements[index];
    }
}