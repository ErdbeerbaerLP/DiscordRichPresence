package de.erdbeerbaerlp.discordrpc;

import com.google.common.base.Predicate;
import com.mojang.logging.LogUtils;
import de.erdbeerbaerlp.discordrpc.client.ClientConfig;
import de.erdbeerbaerlp.discordrpc.client.DRPCEventHandler;
import de.erdbeerbaerlp.discordrpc.client.Discord;
import de.erdbeerbaerlp.discordrpc.client.gui.ConfigGui;
import de.erdbeerbaerlp.discordrpc.server.ServerConfig;
import de.jcm.discordgamesdk.Core;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
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
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Mod("discordrpc")
public class DRPC /*implements IHasConfigGUI*/ {
    /**
     * Mod ID
     */
    public static final String MODID = "discordrpc";
    /**
     * The timestamp when the game was launched
     */
    public static final Instant gameStarted = Instant.now();
    private static final String protVersion = "1.0.0";
    private static final Predicate<String> pred = (ver) -> ver.equals(protVersion) || ver.equals(NetworkRegistry.ACCEPTVANILLA) || ver.equals(NetworkRegistry.ABSENT);
    public static final Logger LOGGER = LogUtils.getLogger();
    protected static final SimpleChannel ICON = NetworkRegistry.newSimpleChannel(new ResourceLocation(DRPC.MODID, "discord-icon"), () -> {
        return protVersion;
    }, pred, pred);
    public static boolean started = false;
    protected static final SimpleChannel MSG = NetworkRegistry.newSimpleChannel(new ResourceLocation(DRPC.MODID, "discord-msg"), () -> protVersion, pred, pred);
    public static boolean isEnabled = true;
    public static boolean isClient = true;
    public static boolean preventConfigLoad = false;

    public DRPC() {
        AtomicBoolean downloaded = new AtomicBoolean(true);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
            try {
                File discordLibrary = downloadDiscordLibrary();
                if (discordLibrary == null) {
                    LOGGER.error("Error downloading Discord SDK.");
                    LOGGER.error("Not loading mod");
                    downloaded.set(false);
                    return;
                }
                // Initialize the Core
                Core.init(discordLibrary);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("Not loading mod");
                downloaded.set(false);
            }
        });
        if (!downloaded.get()) return;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postInit);

        ICON.registerMessage(1, Message_Icon.class, (a, b) -> a.encode(a, b), (a) -> new Message_Icon(readString(a)), (a, b) -> a.onMessageReceived(a, b));
        MSG.registerMessage(1, Message_Message.class, (a, b) -> a.encode(a, b), (a) -> new Message_Message(readString(a)), (a, b) -> a.onMessageReceived(a, b));
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            try {
                ClientConfig.instance().loadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public static File downloadDiscordLibrary() throws IOException {
        // Find out which name Discord's library has (.dll for Windows, .so for Linux)
        String name = "discord_game_sdk";
        String suffix;

        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (osName.contains("windows")) {
            suffix = ".dll";
        } else if (osName.contains("linux")) {
            suffix = ".so";
        } else if (osName.contains("mac os")) {
            suffix = ".dylib";
        } else {
            throw new RuntimeException("cannot determine OS type: " + osName);
        }

		/*
		Some systems report "amd64" (e.g. Windows and Linux), some "x86_64" (e.g. Mac OS).
		At this point we need the "x86_64" version, as this one is used in the ZIP.
		 */
        if (arch.equals("amd64"))
            arch = "x86_64";

        // Path of Discord's library inside the ZIP
        String zipPath = "lib/" + arch + "/" + name + suffix;

        // Open the URL as a ZipInputStream
        URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/2.5.6/discord_game_sdk.zip");
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestProperty("User-Agent", "discord-game-sdk4j (https://github.com/JnCrMx/discord-game-sdk4j)");
        ZipInputStream zin = new ZipInputStream(connection.getInputStream());

        // Search for the right file inside the ZIP
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().equals(zipPath)) {
                // Create a new temporary directory
                // We need to do this, because we may not change the filename on Windows
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "java-" + name + System.nanoTime());
                if (!tempDir.mkdir())
                    throw new IOException("Cannot create temporary directory");
                tempDir.deleteOnExit();

                // Create a temporary file inside our directory (with a "normal" name)
                File temp = new File(tempDir, name + suffix);
                temp.deleteOnExit();

                // Copy the file in the ZIP to our temporary file
                Files.copy(zin, temp.toPath());

                // We are done, so close the input stream
                zin.close();

                // Return our temporary file
                return temp;
            }
            // next entry
            zin.closeEntry();
        }
        zin.close();
        // We couldn't find the library inside the ZIP
        return null;
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

    private static void sendPackets(ServerPlayer p) {
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
        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> sendPackets((ServerPlayer) ev.getEntity()));
    }

    @SubscribeEvent
    public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent ev) {
        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> sendPackets((ServerPlayer) ev.getEntity()));
    }

    private void setup(final FMLCommonSetupEvent event) {
    } //Unused for now

    private void clientSetup(final FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> {
                    if (ClientConfig.instance().configGUIDisabled)
                        return null;
                    else
                        return new ConfigGui(screen);
                }));
        MinecraftForge.EVENT_BUS.register(DRPCEventHandler.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DRPC.LOGGER.info("Shutting down DiscordHook.");
            Discord.shutdown();
        }));
        if (isEnabled) Discord.initDiscord();
        if (isEnabled)
            Discord.setPresence(ClientConfig.instance().name, "Starting game...", "34565655649643693", false);

    }

    public void serverSetup(FMLDedicatedServerSetupEvent event) {
        DRPC.isClient = false;
    }

    public void postInit(InterModProcessEvent event) {

        if (isEnabled && isClient)
            Discord.setPresence(ClientConfig.instance().name, "Starting game...", "3454083453475893469");
        started = true;
    }
/*
    @Override
    public Screen getConfigGUI(Screen screen) {
        return ClientConfig.CONFIG_GUI_DISABLED.get() ? null : new ConfigGui(screen);
    }*/
}
