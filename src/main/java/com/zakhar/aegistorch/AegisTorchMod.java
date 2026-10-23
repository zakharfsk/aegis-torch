package com.zakhar.aegistorch;

import com.zakhar.aegistorch.network.MessageTorchSettings;
import com.zakhar.aegistorch.network.ServerTaskQueue;
import com.zakhar.aegistorch.proxy.CommonProxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Aegis Torch - a torch that lights the area like a normal torch (light 15)
 * and blocks natural hostile mob spawning in a radius that can be set per torch
 * through a GUI (right click). The protected zone can be shown in the world,
 * and hostile mobs inside it can be pushed out.
 */
@Mod(modid = AegisTorchMod.MODID, name = AegisTorchMod.NAME, version = AegisTorchMod.VERSION,
        acceptedMinecraftVersions = "[1.7.10]")
public class AegisTorchMod {

    public static final String MODID = "aegistorch";
    public static final String NAME = "Aegis Torch";
    public static final String VERSION = "1.2.0";

    public static final int GUI_TORCH = 0;

    @Mod.Instance(MODID)
    public static AegisTorchMod instance;

    @SidedProxy(clientSide = "com.zakhar.aegistorch.proxy.ClientProxy",
            serverSide = "com.zakhar.aegistorch.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper network;
    public static BlockAegisTorch aegisTorch;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AegisTorchConfig.load(event.getSuggestedConfigurationFile());

        aegisTorch = new BlockAegisTorch();
        GameRegistry.registerBlock(aegisTorch, "aegis_torch");
        GameRegistry.registerTileEntity(TileAegisTorch.class, MODID + ":aegis_torch");

        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        network.registerMessage(MessageTorchSettings.Handler.class, MessageTorchSettings.class, 0, Side.SERVER);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new SpawnBlocker());
        FMLCommonHandler.instance().bus().register(new ServerTaskQueue());
        FMLCommonHandler.instance().bus().register(new MobPusher());
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        proxy.init();

        if (AegisTorchConfig.enableRecipe) {
            Block torch = GameRegistry.findBlock("minecraft", "torch");
            // T T T
            // D G D    T - torch, D - diamond, G - gold block, L - any log
            //   L
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(aegisTorch, 1),
                    "TTT", "DGD", " L ",
                    'T', torch,
                    'D', "gemDiamond",
                    'G', "blockGold",
                    'L', "logWood"));
        }
    }
}
