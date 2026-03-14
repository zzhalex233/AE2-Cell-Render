package com.zzhalex233.ae2cellrender.client.drive;

final class DriveCellShading {

    private DriveCellShading() {
    }

    static int shadeComponent(int component, float multiplier) {
        int shaded = Math.round(component * multiplier);
        if (shaded < 0) {
            return 0;
        }
        return Math.min(255, shaded);
    }

    static int blendColor(int red, int green, int blue, float multiplier) {
        return (shadeComponent(red, multiplier) << 16)
                | (shadeComponent(green, multiplier) << 8)
                | shadeComponent(blue, multiplier);
    }
}
