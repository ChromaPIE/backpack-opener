package cr.chromapie.bpopener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cr.chromapie.bpopener.config.BPOConfig;
import cr.chromapie.bpopener.proxy.CommonProxy;

@Mod(modid = BPOpenerMod.MODID, name = BPOpenerMod.NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class BPOpenerMod {

    public static final String MODID = "bpopener";
    public static final String NAME = "Backpack Opener";

    private static final String CLIENT_PROXY = "cr.chromapie.bpopener.proxy.ClientProxy";
    private static final String SERVER_PROXY = "cr.chromapie.bpopener.proxy.CommonProxy";

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static CommonProxy proxy;

    private static final Logger LOGGER = LogManager.getLogger(MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        BPOConfig.init(event.getModConfigurationDirectory());
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
