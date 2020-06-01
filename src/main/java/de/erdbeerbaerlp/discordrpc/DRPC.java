package de.erdbeerbaerlp.discordrpc;

import com.google.common.base.Predicate;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;


@Mod("discordrpc")
public class DRPC
{
	/**
	 * Mod ID
	 */
	public static final String MODID = "discordrpc";
	protected static boolean isEnabled = true;
	private static final String protVersion = "1.0.0";
	private static final Predicate<String> pred = (ver) -> ver.equals(protVersion) || ver.equals(NetworkRegistry.ACCEPTVANILLA) || ver.equals(NetworkRegistry.ABSENT);
	protected static final SimpleChannel MSG = NetworkRegistry.newSimpleChannel(new ResourceLocation(DRPC.MODID, "discord-msg"), () -> {
		return protVersion;
	}, pred, pred);
	protected static final SimpleChannel ICON = NetworkRegistry.newSimpleChannel(new ResourceLocation(DRPC.MODID, "discord-icon"), () -> {
		return protVersion;
	}, pred, pred);
	protected static boolean isClient = true;
	public static boolean started = false;
	protected static boolean preventConfigLoad = false;
	/**
	 * The timestamp when the game was launched
	 */
	public static final long gameStarted = Instant.now().toEpochMilli();
	/**
	 * Used for development purposes
	 */
	protected static boolean logtochat = false;

	public DRPC() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postInit);

		ICON.registerMessage(1, Message_Icon.class, (a, b) -> a.encode(a, b), (a) -> new Message_Icon(readString(a)), (a, b) -> a.onMessageReceived(a, b));
		MSG.registerMessage(1, Message_Message.class, (a, b) -> a.encode(a, b), (a) -> new Message_Message(readString(a)), (a, b) -> a.onMessageReceived(a, b));
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			ModLoadingContext.get().registerConfig(Type.COMMON, ClientConfig.CONFIG_SPEC, "DiscordRPC.toml");
			MinecraftForge.EVENT_BUS.register(ClientConfig.class);
			MinecraftForge.EVENT_BUS.addListener(ClientConfig::onFileChange);
			MinecraftForge.EVENT_BUS.addListener(ClientConfig::onLoad);
			MinecraftForge.EVENT_BUS.register(DRPCEventHandler.class);
		});
		DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
			ModLoadingContext.get().registerConfig(Type.COMMON, ServerConfig.CONFIG_SPEC, "DiscordRPC-Server.toml");
			MinecraftForge.EVENT_BUS.register(ServerConfig.class);
			MinecraftForge.EVENT_BUS.addListener(ServerConfig::onFileChange);
			MinecraftForge.EVENT_BUS.addListener(ServerConfig::onLoad);
			MinecraftForge.EVENT_BUS.addListener(DRPC::playerChangeDimension);
			MinecraftForge.EVENT_BUS.addListener(DRPC::playerJoin);
		});
	}
	private static String readString(ByteBuf b) {
		final ArrayList<Byte> list = new ArrayList<>();
		while (b.isReadable()) {
			list.add(b.readByte());
		}
		final byte[] out = new byte[list.size()];
		int i1 = 0;
		if (list.get(0) == 0x0) {
			i1 = 1;
		}
		for (int i = i1; i < list.size(); i++) {
			out[i] = list.get(i);
		}
		return new String(out, StandardCharsets.UTF_8).trim();
	}
	private void setup(final FMLCommonSetupEvent event) {} //Unused for now
	private void clientSetup(final FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(DRPCEventHandler.class);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			DRPCLog.Info("Shutting down DiscordHook.");
			Discord.shutdown();
		}));
		if (isEnabled) Discord.initDiscord();
		if (isEnabled) Discord.setPresence(ClientConfig.NAME.get(), "Starting game...", "34565655649643693", false);

	}

	public void serverSetup(FMLDedicatedServerSetupEvent event) {
		DRPC.isClient = false;
	}

	private static void sendPackets(ServerPlayerEntity p) {
		new Thread(() -> {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			PacketDistributor.PLAYER.with(() -> p).send(DRPC.MSG.toVanillaPacket(new Message_Message(ServerConfig.SERVER_MESSAGE.get()), NetworkDirection.PLAY_TO_CLIENT));
			PacketDistributor.PLAYER.with(() -> p).send(DRPC.ICON.toVanillaPacket(new Message_Icon(ServerConfig.SERVER_ICON.get()), NetworkDirection.PLAY_TO_CLIENT));
		}).start();
	}

	@SubscribeEvent
	public static void playerJoin(PlayerEvent.PlayerLoggedInEvent ev) {
		DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> sendPackets((ServerPlayerEntity) ev.getPlayer()));
	}

	@SubscribeEvent
	public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent ev) {
		DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> sendPackets((ServerPlayerEntity) ev.getPlayer()));
	}

	public void postInit(InterModProcessEvent event) {

		if (isEnabled && isClient)
			Discord.setPresence(ClientConfig.NAME.get(), "Starting game...", "3454083453475893469");
		started = true;
	}

}
