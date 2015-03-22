package blargerist.cake.blockphysics;
 
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import blargerist.cake.blockphysics.events.BPEventHandler;

@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION, dependencies = "", acceptableRemoteVersions = "*")
public class BlockPhysics
{
   
	@Instance("BlockPhysics")
	public static BlockPhysics instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModConfig.init(event.getSuggestedConfigurationFile());
		MinecraftForge.EVENT_BUS.register(new BPEventHandler());
		//FMLCommonHandler.instance().bus().register(new BPEventHandler());
	}

	@EventHandler 
	public void init(FMLInitializationEvent event)
	{
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
	}
}