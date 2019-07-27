package de.erdbeerbaerlp.discordrpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fr.nukerhd.hiveapi.response.games.Games;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class DRPCEventHandler {
    protected static int currentOnline = -1;
    protected static int currentMax = -1;
    protected static boolean inWorld = false;
    protected static String Hypixel_LastGame = "";
    protected static String serverCustomMessage = "";
    protected static boolean usingCustomMsg = false;
    protected static boolean checkedUpdate = false;

    protected static String customIco = "cube";
    protected static int tickAmount = 120;

    @SubscribeEvent
    public static void onConfigurationChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
        if (e.getModID().equalsIgnoreCase(ModClass.MODID)) {
            // Resync configs
            DRPCLog.Info("Configuration has changed");
            RPCconfig.reloadConfig();
        }
    }

    @SubscribeEvent
    public static void onWorldLeave(ClientDisconnectionFromServerEvent event) {

        if (ModClass.isClient && ModClass.isEnabled) {
            resetVars();
            Discord.setPresence(RPCconfig.NAME, "In Main Menu", "cube");
        }

    }

    private static void resetVars() {

        Discord.now = ModClass.gameStarted;
        inWorld = false;
        currentOnline = -1;
        currentMax = -1;
        Hypixel_LastGame = "-";
        serverCustomMessage = "";
        usingCustomMsg = false;
        tickAmount = 0;
        customIco = "cube";

    }

    private static int limboTimes = 0;
    @SubscribeEvent
    @SuppressWarnings("ConstantConditions")
    public static void onTick(PlayerTickEvent event) {

        if (ModClass.isClient) {
            if (!checkedUpdate) {
                CheckResult result = ForgeVersion.getResult(Loader.instance().getIndexedModList().get(ModClass.MODID));
                if (result.status == Status.OUTDATED) {
                    event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c Update available!\n\u00A7cCurrent version: \u00A74" + ModClass.VERSION + "\u00A7c, Newest: \u00A7a" + result.target + "\n\u00A7cChangelog:\n\u00A76" + result.changes.get(result.target)).setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, result.url)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to get newer Version")))));
                    DRPCLog.Fatal("UpdateCheck: Update Available. Download it here: https://minecraft.curseforge.com/projects/discordrichpresence/files");
                    checkedUpdate = true;
                } else if (result.status == Status.AHEAD) {
                    event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A77 It looks like you are using an Development version... \n\u00A77Your version: \u00A76" + ModClass.VERSION).setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, result.url)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to get current stable Version")))));
                    DRPCLog.Fatal("UpdateCheck: Unreleased version... Assuming Development Version");
                    checkedUpdate = true;
                } else if (result.status == Status.FAILED) {
                    event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c FAILED TO CHECK FOR UPDATES"));
                    DRPCLog.Fatal("UpdateCheck: Unable to check for updates");
                    checkedUpdate = true;
                } else if (result.status == Status.BETA) {
                    event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7a You are using an Beta Version. This may contain bugs which are being fixed."));
                    DRPCLog.Fatal("UpdateCheck: Beta");
                    checkedUpdate = true;
                } else if (result.status == Status.BETA_OUTDATED) {
                    event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c You are using an Outdated Beta Version. This may contain bugs which are being fixed or are already fixed\n\u00A76Changelog of newer Beta:" + result.changes.get(result.target)));
                    DRPCLog.Fatal("UpdateCheck: Bata_outdated");
                    checkedUpdate = true;
                } else {
                    DRPCLog.Info("UpdateCheck: " + result.status.toString());
                    checkedUpdate = true;
                }
            }
            if (ModClass.isEnabled) {
                try {
                    int maxPlayers = Minecraft.getMinecraft().getConnection().currentServerMaxPlayers;
                    int online = Minecraft.getMinecraft().getConnection().getPlayerInfoMap().size();
                    if (!usingCustomMsg && !serverCustomMessage.equals("") && RPCconfig.ENABLE_CUSTOM_INTEGRATION) {
                        DRPCLog.Debug("CustomMSG Applied");
                        Discord.setPresence(RPCconfig.NAME,
                                serverCustomMessage
                                        .replace("%players%", online + "")
                                        .replace("%otherpl%", (online - 1) + "")
                                        .replace("%dimensionName%", Minecraft.getMinecraft().world.provider.getDimensionType().getName())
                                        .replace("%dimensionID%", Minecraft.getMinecraft().world.provider.getDimensionType().getId() + "")
                                        .replace("%biome%", Minecraft.getMinecraft().player.world.getBiomeForCoordsBody(Minecraft.getMinecraft().player.getPosition()).getBiomeName())
                                , customIco);
                        usingCustomMsg = true;
                    }
                    if (Minecraft.getMinecraft().getCurrentServerData() == null) {
                        if (tickAmount <= 0) {
                            World world = Minecraft.getMinecraft().world;
                            IntegratedServer iServer = Minecraft.getMinecraft().getIntegratedServer();
                            EntityPlayerSP player = Minecraft.getMinecraft().player;
                            int posX = Double.valueOf(player.posX).intValue();
                            int posY = Double.valueOf(player.posY).intValue();
                            int posZ = Double.valueOf(player.posZ).intValue();
                            Discord.setPresence(RPCconfig.NAME,
                                    RPCconfig.WORLD_MESSAGE
                                            .replace("%world%", iServer.getFolderName())
                                            .replace("%coords%", "X:" + posX + " Y:" + posY + " Z:" + posZ)
                                            .replace("%dimensionName%", Minecraft.getMinecraft().world.provider.getDimensionType().getName())
                                            .replace("%dimensionID%", Minecraft.getMinecraft().world.provider.getDimensionType().getId() + "")
                                            .replace("%biome%", player.world.getBiomeForCoordsBody(player.getPosition()).getBiomeName())

                                    , "world");
                            tickAmount = 100;
                        } else
                            tickAmount--;
                    } else {
                        //System.out.println("Tick #"+tickAmount);
                        if (tickAmount == 0) {
                            tickAmount = 500;
                            currentMax = maxPlayers;
                            currentOnline = online;
                            if (!serverCustomMessage.isEmpty()) {
                                DRPCLog.Debug("CustomMSG Applied");
                                Discord.setPresence(RPCconfig.NAME, serverCustomMessage.replace("%players%", online + "").replace("%otherpl%", (online - 1) + ""), customIco);
                                usingCustomMsg = true;
                            } else {
                                if (Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net") && RPCconfig.ENABLE_HYPIXEL_INTEGRATION) {
                                    String scoreboardTitle;
                                    try {
                                        scoreboardTitle = removeFormatting(Minecraft.getMinecraft().world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName());
                                    } catch (NullPointerException e) {
                                        scoreboardTitle = "";
                                    }
                                    if (scoreboardTitle.equals("")) {
                                        if (limboTimes >= 10)
                                            Discord.setPresence("Hypixel", "In Limbo", "49tz49873897485");
                                        limboTimes++;
                                        return;
                                    }
                                    limboTimes = 0;
                                    if (scoreboardTitle.equals("HYPIXEL") || scoreboardTitle.equals("PROTOTYPE") || scoreboardTitle.equals("THE TNT GAMES") || scoreboardTitle.equals("ARCADE GAMES") || scoreboardTitle.equals("CLASSIC GAMES"))
                                        scoreboardTitle = "In hub";
                                    if (!scoreboardTitle.equals(Hypixel_LastGame))
                                        Discord.now = Instant.now().getEpochSecond();
                                    Hypixel_LastGame = scoreboardTitle;
                                    final List<String> scoreboardLines = ScoreboardUtils.getSidebarScores(Minecraft.getMinecraft().world.getScoreboard());
                                    if (scoreboardTitle.startsWith("SKYBLOCK")) {
                                        for (String line : ScoreboardUtils.getSidebarScores(Minecraft.getMinecraft().world.getScoreboard())) {
                                            line = removeFormatting(line).trim();
//											System.out.println(line);
                                            if (removeFormatting(line).startsWith("\u23E3")) {
                                                line = line.replace("\u23E3", "").trim();
                                                if (line.equalsIgnoreCase("none"))
                                                    Discord.setPresence("Hypixel", "Skyblock - Unknown place", "49tz49873897485");
                                                else if (line.equalsIgnoreCase("Your island"))
                                                    Discord.setPresence("Hypixel", "Skyblock - Private Island", "49tz49873897485");
                                                else
                                                    Discord.setPresence("Hypixel", "Skyblock - " + line + " (with " + (online - 1) + " players)", "49tz49873897485");
                                                break;
                                            }

                                        }
                                        return;
                                    }
                                    if (scoreboardTitle.equals("BED WARS")) {
                                        String text = "BedWars - Unknown state! Report to mod author!";
                                        for (String line : scoreboardLines) {
                                            line = removeFormatting(line).trim();
                                            if (line.contains("Loot Chests:")) {
                                                text = "BedWars Hub";
                                                break;
                                            } else if (line.contains("Mode:")) {
                                                text = "BedWars Lobby - Mode: " + line.replace("Mode:", "").trim() + " - " + removeFormatting(scoreboardLines.get(5));
                                                break;
                                            } else if (line.contains("YOU")) {
                                                final boolean hasBed = line.contains("✓");
                                                final boolean loose = line.contains("✗");
                                                if (loose)
                                                    text = "Bedwars - Game Over";
                                                else {
                                                    final String t = StringUtils.substringBetween(line, ": ", " YOU");
                                                    text = "BedWars - Team " + line.substring(2).replace(": ", "").replace(" YOU", "").replace(t, "").trim() + " - Bed: " + (hasBed ? "Standing" : "Destroyed") + " - " + removeFormatting(scoreboardLines.get(4));
                                                }
                                                break;
                                            } else if (line.contains("Beds Broken"))
                                                text = "Bedwars - Game Over";

                                        }
                                        DRPCLog.Debug(text);
                                        if (text.equals("BedWars - Unknown state! Report to mod author!")) {
                                            DRPCLog.Fatal("Unknown State!!!");
                                            for (final String l : scoreboardLines) {
                                                DRPCLog.Fatal(removeFormatting(l));
                                            }
                                        }
                                        Discord.setPresence("Hypixel", text, "49tz49873897485");
                                        return;
                                    }
                                    if (scoreboardTitle.equals("FARM HUNT")) {
                                        for (String s : scoreboardLines) {
                                            s = removeFormatting(s).trim();
                                            if (s.contains("Players:")) {
                                                Discord.setPresence("Hypixel", "Farm Hunt - Lobby", "49tz49873897485");
                                                return;
                                            }
                                            if (s.equals("Hunter")) {
                                                Discord.setPresence("Hypixel", "Farm Hunt - In Game - Hunter", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Disguise:")) {
                                                Discord.setPresence("Hypixel", "Farm Hunt - In Game - Animal", "49tz49873897485");
                                                return;
                                            }
                                        }
                                    }
                                    if (scoreboardTitle.equals("HOUSING")) {
                                        for (int i = 0; i < scoreboardLines.size(); i++) {
                                            final String s = removeFormatting(scoreboardLines.get(i)).trim();
                                            if (s.equals("House Name:")) {
                                                Discord.setPresence("Hypixel", "Housing - Visiting " + removeFormatting(scoreboardLines.get(i - 1)), "49tz49873897485");
                                                return;
                                            }
                                            if (s.equals("Visiting Rules:")) {
                                                Discord.setPresence("Hypixel", "Housing - At Home", "49tz49873897485");
                                                return;
                                            }
                                        }

                                    }
                                    if (scoreboardTitle.equals("TURBO KART RACERS")) {
                                        for (String s : scoreboardLines) {
                                            s = removeFormatting(s).trim();
                                            if (s.contains("Map:")) {
                                                Discord.setPresence("Hypixel", "Turbo Kart Racers - Lobby", "49tz49873897485");
                                                return;
                                            }
                                        }
                                        for (Score s : Minecraft.getMinecraft().world.getScoreboard().getScores()) {
                                            if (s.getObjective().equals(Minecraft.getMinecraft().world.getScoreboard().getObjectiveInDisplaySlot(1))) {
                                                System.out.println(removeFormatting(s.getPlayerName()));
                                                if (removeFormatting(s.getPlayerName()).contains(Minecraft.getMinecraft().player.getDisplayNameString().substring(0, 12).trim())) {
                                                    Discord.setPresence("Hypixel", "Turbo Kart Racers - In Game - Position: " + removeFormatting(s.getPlayerName()).charAt(0), "49tz49873897485");
                                                    return;
                                                }
                                                if (removeFormatting(s.getPlayerName()).contains("You're")) {
                                                    Discord.setPresence("Hypixel", "Turbo Kart Racers - Finished - Position: " + removeFormatting(s.getPlayerName()).replace("You're #", "").trim(), "49tz49873897485");
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                    if (scoreboardTitle.equals("SKYWARS")) {
                                        for (String s : scoreboardLines) {
                                            s = removeFormatting(s).trim();
                                            if (s.contains("Loot Chests:")) {
                                                Discord.setPresence("Hypixel", "SkyWars - Hub", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Players:")) {
                                                Discord.setPresence("Hypixel", "SkyWars - Lobby", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Kills:")) {
                                                Discord.setPresence("Hypixel", "SkyWars - In Game - " + s, "49tz49873897485");
                                                return;
                                            }
                                        }
                                    }
                                    if (scoreboardTitle.equals("MEGA WALLS")) {
                                        for (String s : scoreboardLines) {
                                            s = removeFormatting(s).trim();
                                            if (s.contains("Coins:")) {
                                                Discord.setPresence("Hypixel", "Mega Walls - Hub", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Players:")) {
                                                Discord.setPresence("Hypixel", "Mega Walls - Lobby", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Kills / Assists:")) {
                                                Discord.setPresence("Hypixel", "Mega Walls - In Game - " + s, "49tz49873897485");
                                                return;
                                            }
                                        }
                                    }
                                    if (scoreboardTitle.equals("BUILD BATTLE")) {
                                        for (String s : scoreboardLines) {
                                            s = removeFormatting(s).trim();
                                            if (s.contains("Coins:")) {
                                                Discord.setPresence("Hypixel", "Build Battle - Hub", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Starting in") || s.contains("Waiting...")) {
                                                Discord.setPresence("Hypixel", "Build Battle - Lobby", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Vote Now")) {
                                                Discord.setPresence("Hypixel", "Build Battle - Voting Theme", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Time:")) {
                                                Discord.setPresence("Hypixel", "Build Battle - In Game - " + s, "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Vote:")) {
                                                Discord.setPresence("Hypixel", "Build Battle - Voting", "49tz49873897485");
                                                return;
                                            }
                                        }
                                    }
                                    if (scoreboardTitle.equals("MURDER MYSTERY")) {
                                        for (String s : scoreboardLines) {
                                            s = removeFormatting(s);
                                            if (s.contains("Loot Chests:")) {
                                                Discord.setPresence("Hypixel", "Murder Mystery - Hub", "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Mode:")) {
                                                Discord.setPresence("Hypixel", "Murder Mystery - Game Lobby - " + s, "49tz49873897485");
                                                return;
                                            }
                                            if (s.contains("Role:")) {
                                                Discord.setPresence("Hypixel", "Murder Mystery - In Game - " + s + " - " + removeFormatting(scoreboardLines.get(6)), "49tz49873897485");
                                                return;
                                            }
                                        }
                                        return;
                                    }
                                    if (scoreboardTitle.equals("THE HYPIXEL PIT")) {
                                        String text = "";
                                        for (final String s : scoreboardLines) {
                                            if (s.contains("Status:")) {
                                                final int level = removeFormatting(s).equalsIgnoreCase("Status: Event") ? 6 : 7;
                                                text = removeFormatting(s + " - " + scoreboardLines.get(level).replace("[", "").replace("]", "")).trim();
                                            }
                                        }
                                        Discord.setPresence("Hypixel", "The Pit - " + text, "49tz49873897485");
                                        return;
                                    }
                                    Discord.setPresence("Hypixel", scoreboardTitle + " with " + (online - 1) + " other players", "49tz49873897485");
                                } else if (Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("mineplex.com") && RPCconfig.ENABLE_CUSTOM_INTEGRATION) {
                                    Discord.setPresence(RPCconfig.NAME, "Playing on Mineplex with " + (online - 1) + " other players", "23498365347867869");
                                } else if (Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("wynncraft.com") && RPCconfig.ENABLE_CUSTOM_INTEGRATION) {
                                    Discord.setPresence(RPCconfig.NAME, "Playing on Wynncraft, The Minecraft MMORPG", "4878hz4389634tz987");
                                } else if (Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hivemc.com") && RPCconfig.ENABLE_HIVEMC_INTEGRATION) {
                                    try {
                                        JsonParser parse = new JsonParser();
                                        URL gameURL = new URL("https://api.hivemc.com/v1/player/" + Minecraft.getMinecraft().player.getName() + "/status/raw?v=1");
                                        HttpURLConnection gameConn = (HttpURLConnection) gameURL.openConnection();
                                        gameConn.setRequestProperty("User-Agent", "ErdbeerbaerLP-DiscordRichPresence-Mod");
                                        InputStream isGame = gameConn.getInputStream();
                                        BufferedReader r = new BufferedReader(new InputStreamReader(isGame, Charset.forName("UTF-8")));
                                        String result = r.lines().collect(Collectors.joining());
                                        JsonElement json = parse.parse(result);
                                        gameConn.disconnect();
                                        String gameS = json.getAsJsonObject().get("status").getAsString();
                                        String gameO;
                                        try {
                                            gameO = Games.valueOf(gameS).getName();
                                        } catch (IllegalArgumentException e) {
                                            gameO = gameS;
                                        }
                                        if (gameO.equals("HUB")) gameO = "in HUB";
                                        if (gameO.equals("BEDT")) gameO = "BedWars";
                                        Discord.setPresence("TheHive", "Playing " + gameO + " on hivemc.com", "38462896734683686");
                                    } catch (Exception ignored) {
                                    }


                                } else {

                                    int posX = Double.valueOf(Minecraft.getMinecraft().player.posX).intValue();
                                    int posY = Double.valueOf(Minecraft.getMinecraft().player.posY).intValue();
                                    int posZ = Double.valueOf(Minecraft.getMinecraft().player.posZ).intValue();
                                    Discord.setPresence(RPCconfig.NAME,
                                            RPCconfig.SERVER_MESSAGE
                                                    .replace("%ip%", Minecraft.getMinecraft().getCurrentServerData().serverIP)
                                                    .replace("%dimensionName%", Minecraft.getMinecraft().world.provider.getDimensionType().getName())
                                                    .replace("%dimensionID%", Minecraft.getMinecraft().world.provider.getDimensionType().getId() + "")
                                                    .replace("%biome%", Minecraft.getMinecraft().player.world.getBiomeForCoordsBody(Minecraft.getMinecraft().player.getPosition()).getBiomeName())
                                                    .replace("%online%", online + "")
                                                    .replace("%max%", maxPlayers + "")
                                                    .replace("%otherpl%", (online - 1) + "")
                                                    .replace("%coords%", "X:" + posX + " Y:" + posY + " Z:" + posZ)

                                            , "cube");
                                }
                            }
                        } else if (tickAmount < 0) tickAmount = 2000;
                        else tickAmount--;
                    }
                } catch (Exception e) {
                    DRPCLog.Error("(Usually not a bug) ERROR in onPlayerTick... " + e);

                }
            }
        }

    }

    /**
     * Removes Color code formatting
     *
     * @param formatted Formatted text with §2 color codes
     * @return Raw text without color codes
     */
    public static String removeFormatting(String formatted) {
        return formatted
                .replaceAll("\u00A70", "")
                .replaceAll("\u00A71", "")
                .replaceAll("\u00A72", "")
                .replaceAll("\u00A73", "")
                .replaceAll("\u00A74", "")
                .replaceAll("\u00A75", "")
                .replaceAll("\u00A76", "")
                .replaceAll("\u00A77", "")
                .replaceAll("\u00A78", "")
                .replaceAll("\u00A79", "")
                .replaceAll("\u00A7a", "")
                .replaceAll("\u00A7b", "")
                .replaceAll("\u00A7c", "")
                .replaceAll("\u00A7d", "")
                .replaceAll("\u00A7e", "")
                .replaceAll("\u00A7f", "")
                .replaceAll("\u00A7l", "")
                .replaceAll("\u00A7k", "")
                .replaceAll("\u00A7m", "")
                .replaceAll("\u00A7n", "")
                .replaceAll("\u00A7o", "")
                .replaceAll("\u00A7r", "")
                .replaceAll("\u00A7A", "")
                .replaceAll("\u00A7B", "")
                .replaceAll("\u00A7C", "")
                .replaceAll("\u00A7D", "")
                .replaceAll("\u00A7E", "")
                .replaceAll("\u00A7F", "")
                .replaceAll("\u00A7L", "")
                .replaceAll("\u00A7K", "")
                .replaceAll("\u00A7M", "")
                .replaceAll("\u00A7N", "")
                .replaceAll("\u00A7O", "")
                .replaceAll("\u00A7R", "");
    }

    @SubscribeEvent
    public static void onMenuOpened(GuiOpenEvent event) {

        if (ModClass.isClient && ModClass.isEnabled) {
            String guiName;
            if (event.getGui() != null) {
                guiName = event.getGui().toString().split("@")[0];
                DRPCLog.Debug("GUI: " + guiName);
            }
            if ((event.getGui() instanceof GuiMainMenu || event.getGui() instanceof GuiMultiplayer) && !inWorld) {
                resetVars();
                checkedUpdate = false;
                Discord.setPresence(RPCconfig.NAME, "In Main Menu", "cube");
            } else if (event.getGui() instanceof GuiDownloadTerrain) {
                if (RPCconfig.ENABLE_CUSTOM_INTEGRATION)
                    ModClass.REQUEST.sendToServer(new RequestMessage("DRPC-Message-Request"));
                currentOnline = -1;
                currentMax = -1;
                if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                    if (Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")) {
                        resetVars();
                    }
                }
            }
        }

    }
}
