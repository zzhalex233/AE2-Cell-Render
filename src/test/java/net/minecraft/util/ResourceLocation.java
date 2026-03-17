package net.minecraft.util;

public class ResourceLocation {
    private final String domain;
    private final String path;

    public ResourceLocation(String domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }
}
