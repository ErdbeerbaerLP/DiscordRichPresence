package de.erdbeerbaerlp.discordrpc;

import java.time.Instant;

import com.google.common.base.Predicate;

import net.arikia.dev.drpc.DiscordRPC;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
@Mod("discordrpc")
public class DRPC {
	/**
	 * Mod ID
	 */
	public static final String MODID = "discordrpc";
	/**
	 * Mod Version
	 */
	public static final String VERSION = "2.0";
	/**
	 * Mod Name (What did you expect?)
	 */
	public static final String NAME = "DiscordRichPresence";
	protected static boolean isEnabled = true;
	
//    protected static final SimpleNetworkWrapper REQUEST = NetworkRegistry.newSimpleChannel("DiscordReq");
//    protected static final SimpleNetworkWrapper MSG = NetworkRegistry.INSTANCE.newSimpleChannel("DiscordMSG");
//    protected static final SimpleNetworkWrapper ICO = NetworkRegistry.INSTANCE.newSimpleChannel("DiscordIcon");
	private static final String protVersion = "1.0.0";
	private static final Predicate<String> pred = (ver) -> {return ver.equals(protVersion);};
	protected static final SimpleChannel REQUEST = NetworkRegistry.newSimpleChannel(new ResourceLocation(DRPC.MODID, "discord-req"), ()->{return protVersion;}, pred, pred);
	protected static final SimpleChannel MSG = NetworkRegistry.newSimpleChannel(new ResourceLocation(DRPC.MODID, "discord-msg"), ()->{return protVersion;}, pred, pred);
	protected static final SimpleChannel ICON = NetworkRegistry.newSimpleChannel(new ResourceLocation(DRPC.MODID, "discord-icon"), ()->{return protVersion;}, pred, pred);
	protected static boolean isClient = true;
	protected static boolean logtochat = true;
	protected static boolean preventConfigLoad = false;
	/**
	 * The timestamp when the game was launched
	 */
	public static final long gameStarted = Instant.now().getEpochSecond();
    public DRPC() {
    	System.out.println("Mod Constructor");
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postInit);
    	ICON.<Message_Icon>registerMessage(0, Message_Icon.class,(a, b) -> a.encode(a,b), (a) -> {a.readInt();return new Message_Icon(a.readString(300));}, (a, b) -> a.onMessageReceived(a,b));
    	REQUEST.<RequestMessage>registerMessage(1, RequestMessage.class, (a, b) -> a.encode(a,b), (a) -> {a.readInt();return new RequestMessage(a.readString(300));}, (a, b) -> a.onMessageReceived(a,b));
    	MSG.<Message_Message>registerMessage(1, Message_Message.class, (a, b) -> a.encode(a,b), (a) -> {a.readInt();return new Message_Message(a.readString(300));}, (a, b) -> a.onMessageReceived(a,b));

        
	}
    private void setup(final FMLCommonSetupEvent event) {
    	DRPCLog.Info("CommonSetupEvent");
    }
    private void clientSetup(final FMLClientSetupEvent event) {
    	DRPCLog.Info("ClientSetupEvent");

    	ModLoadingContext.get().registerConfig(Type.COMMON, ClientConfig.CONFIG_SPEC);
    	MinecraftForge.EVENT_BUS.register(ClientConfig.class);
    	MinecraftForge.EVENT_BUS.register(DRPCEventHandler.class);
    	Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			DRPCLog.Info("Shutting down DiscordHook.");
			DiscordRPC.discordShutdown();
		}));

    	
    	if(isEnabled) Discord.initDiscord();
        if(isEnabled) Discord.setPresence(ClientConfig.NAME.get(), "Starting game...", "34565655649643693", false);
    }
    public void serverSetup(FMLDedicatedServerSetupEvent event) {
    	DRPC.isClient = false;
    	ModLoadingContext.get().registerConfig(Type.SERVER, ServerConfig.CONFIG_SPEC);
    	MinecraftForge.EVENT_BUS.register(ServerConfig.class);
    	System.out.println(ServerConfig.SERVER_ICON.get());
    }
	public void postInit(InterModProcessEvent event) {
		if(isEnabled && isClient) Discord.setPresence(ClientConfig.NAME.get(), "Starting game...", "3454083453475893469");
		
	}
}
