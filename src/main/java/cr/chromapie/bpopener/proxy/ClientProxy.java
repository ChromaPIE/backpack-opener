package cr.chromapie.bpopener.proxy;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr.chromapie.bpopener.command.BPOCommand;
import cr.chromapie.bpopener.handler.BPOHandler;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {}

    @Override
    public void init() {
        BPOHandler handler = new BPOHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance()
            .bus()
            .register(handler);

        ClientCommandHandler.instance.registerCommand(new BPOCommand());
    }

    @Override
    public void postInit() {}
}
