package net.minecraft.client.renderer.block.model;

public class ModelResourceLocation {
    private final String domain;
    private final String path;

    public ModelResourceLocation(String domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    public String toString() {
        return domain + ':' + path;
    }
}
