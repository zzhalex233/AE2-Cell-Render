package com.zzhalex233.ae2cellrender;

import com.zzhalex233.ae2cellrender.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = AE2CellRender.MOD_ID,
        name = AE2CellRender.MOD_NAME,
        version = AE2CellRender.VERSION,
        dependencies = "required-after:appliedenergistics2;required-after:mixinbooter"
)
public final class AE2CellRender {

    public static final String MOD_ID = "ae2cellrender";
    public static final String MOD_NAME = "AE2 Cell Render";
    public static final String VERSION = "2.0";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @SidedProxy(
            clientSide = "com.zzhalex233.ae2cellrender.proxy.ClientProxy",
            serverSide = "com.zzhalex233.ae2cellrender.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
    }
}
