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

    public String getNamespace() {
        return domain;
    }

    public String getResourceDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public String getResourcePath() {
        return path;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResourceLocation)) {
            return false;
        }
        ResourceLocation that = (ResourceLocation) other;
        return domain.equals(that.domain) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        int result = domain.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }
}
