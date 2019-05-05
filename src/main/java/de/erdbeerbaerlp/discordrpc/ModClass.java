package de.erdbeerbaerlp.discordrpc;

import java.net.MalformedURLException;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import de.erdbeerbaerlp.discordrpc.RequestMessage.CommunicationMessageHandler;
import de.erdbeerbaerlp.discordrpc.Message_Icon.ICOReceiveHandler;
import de.erdbeerbaerlp.discordrpc.Message_Message.MSGReceiveHandler;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.libraries.Artifact;

@Mod(modid = ModClass.MODID, name = ModClass.NAME, version = ModClass.VERSION, canBeDeactivated = false, acceptedMinecraftVersions = "[1.12,1.12.2]", acceptableRemoteVersions = "*", guiFactory = "de.erdbeerbaerlp.discordrpc.CfgGuiFactory", updateJSON = "http://erdbeerbaerapi.tk/discordrpc.json")
public class ModClass {
	/**
	 * Mod ID
	 */
	public static final String MODID = "discordrpc";
	/**
	 * Mod Version
	 */
	public static final String VERSION = "1.2.2";
	/**
	 * Mod Name (What did you expect?)
	 */
	public static final String NAME = "DiscordRichPresence";
	protected static boolean isEnabled = true;
    protected static final SimpleNetworkWrapper REQUEST = NetworkRegistry.INSTANCE.newSimpleChannel("DiscordReq");
    protected static final SimpleNetworkWrapper MSG = NetworkRegistry.INSTANCE.newSimpleChannel("DiscordMSG");
    protected static final SimpleNetworkWrapper ICO = NetworkRegistry.INSTANCE.newSimpleChannel("DiscordIcon");
    protected static boolean isClient = true;
	protected static boolean logtochat = false;
	protected static boolean preventConfigLoad = false;
	/**
	 * The timestamp where the game was launched
	 */
	public static final long gameStarted = Instant.now().getEpochSecond();
	
	@EventHandler
	public void modConstruction(FMLConstructionEvent evt){
		System.out.println("Constructing");
			if(evt.getSide() == Side.CLIENT){
				DRPCLog.Info("Running on Client side... starting");
				isClient = true;
				if(!preventConfigLoad) RPCconfig.loadConfigFromFile();
				DRPCLog.Debug("Player UUID is "+ Minecraft.getMinecraft().getSession().getPlayerID());
				REQUEST.registerMessage(CommunicationMessageHandler.class, RequestMessage.class, 0, Side.SERVER);
				MSG.registerMessage(MSGReceiveHandler.class, Message_Message.class, 1, Side.CLIENT);
				ICO.registerMessage(ICOReceiveHandler.class, Message_Icon.class, 1, Side.CLIENT);
				if(isEnabled) Discord.initDiscord();
				if(!preventConfigLoad) RPCconfig.loadConfigFromFile();
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					DRPCLog.Info("Shutting down DiscordHook.");
					DiscordRPC.discordShutdown();
				}));
				
				if(isEnabled)Discord.setPresence(RPCconfig.NAME, "Starting game...", "34565655649643693");
				DRPCLog.Debug("Is player developer of this mod? "+Discord.isPlayerDev() != null ? "Yes":"No");
			}else{
				isClient = false;
				DRPCLog.Info("Loading serverside stuff...");
				ServerConfig.preInit();
				REQUEST.registerMessage(CommunicationMessageHandler.class, RequestMessage.class, 0, Side.SERVER);
				MSG.registerMessage(MSGReceiveHandler.class, Message_Message.class, 1, Side.CLIENT);
				ICO.registerMessage(ICOReceiveHandler.class, Message_Icon.class, 1, Side.CLIENT);
			
		}
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		ModMetadata meta = evt.getModMetadata();
		meta.autogenerated = false;
		meta.modId = MODID;
		meta.name = NAME;
		meta.authorList.add("ErdbeerbaerLP");
		meta.description = "Gives you an nice discord rich presence";
		meta.version = VERSION;
		meta.url = "https://minecraft.curseforge.com/projects/discordrichpresence";
		meta.updateJSON = "http://erdbeerbaerapi.tk/discordrpc.json";
			if(isClient && isEnabled){
				MinecraftForge.EVENT_BUS.register(DRPCEventHandler.class);
				ClientCommandHandler.instance.registerCommand(new Command());
			
			}
	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		

			if(isClient && isEnabled){
			Discord.setPresence(RPCconfig.NAME, "Starting game...", "3454083453475893469");
			}
		
	}

	@EventHandler
	public void starting(FMLServerStartingEvent e) {
		e.registerServerCommand(new ServerCommand());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
	}
}
