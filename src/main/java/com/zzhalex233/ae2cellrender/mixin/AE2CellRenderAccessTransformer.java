package com.zzhalex233.ae2cellrender.mixin;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public final class AE2CellRenderAccessTransformer extends AccessTransformer {

    public AE2CellRenderAccessTransformer() throws IOException {
        super("ae2cellrender_at.cfg");
    }
}
