package com.zzhalex233.ae2cellrender.client.drive;

final class DriveCellRenderGate {

    private DriveCellRenderGate() {
    }

    static boolean shouldRenderColorLayer(boolean powered) {
        return powered;
    }
}
