package com.zzhalex233.ae2cellrender.proxy;

import com.zzhalex233.ae2cellrender.network.AE2CellRenderNetwork;

public class CommonProxy {

    public void preInit() {
        AE2CellRenderNetwork.register();
    }
}
