package cr.chromapie.bpopener;

import cr.chromapie.bpopener.command.BPOCommand;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cr.chromapie.bpopener.config.BPOConfig;
import cr.chromapie.bpopener.handler.BPOHandler;

@Mod(modid = BPOpenerMod.MODID, name = BPOpenerMod.NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class BPOpenerMod {

    public static final String MODID = "bpopener";
    public static final String NAME = "Backpack Opener";

    private static final Logger LOGGER = LogManager.getLogger(MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        BPOConfig.init(event.getModConfigurationDirectory());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        BPOHandler handler = new BPOHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance()
            .bus()
            .register(handler);

        // 只在客户端注册命令，避免类加载问题
        if (FMLCommonHandler.instance()
            .getSide() == Side.CLIENT) {
            registerClientCommand();
        }
    }

    /**
     * 注册客户端命令。
     * 独立方法确保 ClientCommandHandler 类只在客户端侧被加载。
     */
    private void registerClientCommand() {
        net.minecraftforge.client.ClientCommandHandler.instance
            .registerCommand(new BPOCommand());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}

    public static Logger getLogger() {
        return LOGGER;
    }
}
