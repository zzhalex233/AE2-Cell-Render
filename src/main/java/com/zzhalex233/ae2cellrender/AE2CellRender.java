package com.zzhalex233.ae2cellrender;

import com.zzhalex233.ae2cellrender.config.AE2CellRenderConfig;
import com.zzhalex233.ae2cellrender.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        dependencies = "required-after:appliedenergistics2;required-after:mixinbooter"
)
public final class AE2CellRender {

    public static final String MOD_ID = Reference.MOD_ID;
    public static final String MOD_NAME = Reference.MOD_NAME;
    public static final String VERSION = Reference.VERSION;
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @SidedProxy(
            modId = Reference.MOD_ID,
            clientSide = "com.zzhalex233.ae2cellrender.proxy.ClientProxy",
            serverSide = "com.zzhalex233.ae2cellrender.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AE2CellRenderConfig.load(event.getSuggestedConfigurationFile());
        proxy.preInit();
    }
}
