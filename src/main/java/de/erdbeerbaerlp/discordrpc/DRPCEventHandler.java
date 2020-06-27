package de.erdbeerbaerlp.discordrpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;
import fr.nukerhd.hiveapi.response.games.Games;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.DownloadTerrainScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.scoreboard.Score;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private static int limboTimes = 0;

    @SubscribeEvent
    public static void onWorldLeave(WorldEvent.Unload event) {
        if (DRPC.isClient && DRPC.isEnabled) {
            resetVars();
            Discord.setPresence(ClientConfig.NAME.get(), ClientConfig.MENU_TEXT.get(), "cube");
        }

    }

    private static void resetVars() {
        Discord.now = DRPC.gameStarted;
        inWorld = false;
        currentOnline = -1;
        currentMax = -1;
        Hypixel_LastGame = "-";
        serverCustomMessage = "";
        usingCustomMsg = false;
        tickAmount = 0;
        customIco = "cube";

    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {

        if (DRPC.isClient) {
            //Update checker is currently not working as expected...
            //				if(checkedUpdate == false){
            //					Status result = ForgeVersion.getStatus();
            //					DRPCLog.Info(result.toString());
            //					if (result == Status.OUTDATED)
            //					{
            //						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c Update available!\n\u00A7cCurrent version: \u00A74"+DRPC.VERSION+"\u00A7c, Newest: \u00A7a"+ForgeVersion.getTarget()+"\n\u00A7cChangelog:\n\u00A76"+ForgeVersion.getSpec()).setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://minecraft.curseforge.com/projects/discordrpc")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to get newer Version")))));
            //						DRPCLog.Fatal("UpdateCheck: Update Available. Download it here: https://minecraft.curseforge.com/projects/discordrichpresence/files");
            //						checkedUpdate = true;
            //					}else if(result == Status.AHEAD){
            //						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A77 It looks like you are using an Development version... \n\u00A77Your version: \u00A76"+DRPC.VERSION).setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://minecraft.curseforge.com/projects/discordrpc")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to get current stable Version")))));
            //						DRPCLog.Fatal("UpdateCheck: Unreleased version... Assuming Development Version");
            //						checkedUpdate = true;
            //					}else if(result == Status.FAILED){
            //						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c FAILED TO CHECK FOR UPDATES"));
            //						DRPCLog.Fatal("UpdateCheck: Unable to check for updates");
            //						checkedUpdate = true;
            //					}else if(result == Status.BETA){
            //						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7a You are using an Beta Version. This may contain bugs which are being fixed."));
            //						DRPCLog.Fatal("UpdateCheck: Beta");
            //						checkedUpdate = true;
            //					}else if(result == Status.BETA_OUTDATED){
            //						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c You are using an Outdated Beta Version. This may contain bugs which are being fixed or are already fixed\n\u00A76Changelog of newer Beta:"+ForgeVersion.getSpec()));
            //						DRPCLog.Fatal("UpdateCheck: Bata_outdated");
            //						checkedUpdate = true;
            //					} else {
            //						DRPCLog.Info("UpdateCheck: "+result.toString());
            //						checkedUpdate = true;
            //					}
            //				}
            if (DRPC.isEnabled) {
                try {

                    int maxPlayers = -1;
                    if (Minecraft.getInstance().getCurrentServerData() != null && Minecraft.getInstance().getCurrentServerData().populationInfo != null && !Minecraft.getInstance().getCurrentServerData().populationInfo.getString().isEmpty() && Minecraft.getInstance().getCurrentServerData().populationInfo.getString().split("/")[1] != null) {
                        maxPlayers = Integer.parseInt(removeFormatting(Minecraft.getInstance().getCurrentServerData().populationInfo.getString().split("/")[1].trim()));
                    }
                    int online = Minecraft.getInstance().getConnection().getPlayerInfoMap().size();

                    if (!usingCustomMsg && !serverCustomMessage.equals("")) {
                        Discord.setPresence(ClientConfig.NAME.get(), serverCustomMessage.replace("%players%", online + "").replace("%otherpl%", (online - 1) + ""), customIco);
                        usingCustomMsg = true;
                    }
                    if (Minecraft.getInstance().getCurrentServerData() == null) {
                        if (tickAmount <= 0) {
                            IntegratedServer iServer = Minecraft.getInstance().getIntegratedServer();
                            ClientPlayerEntity player = Minecraft.getInstance().player;
                            int posX = Double.valueOf(player.lastTickPosX).intValue();
                            int posY = Double.valueOf(player.lastTickPosY).intValue();
                            int posZ = Double.valueOf(player.lastTickPosZ).intValue();
                            Discord.setPresence(ClientConfig.NAME.get(), ClientConfig.WORLD_MESSAGE.get().replace("%world%", iServer.func_240793_aU_().getWorldName()).replace("%coords%", "X:" + posX + " Y:" + posY + " Z:" + posZ), "world");
                            tickAmount = 100;
                        } else
                            tickAmount--;
                    } else {
                        if (tickAmount == 0) {
                            currentMax = maxPlayers;
                            currentOnline = online;
                            if (!serverCustomMessage.isEmpty()) {
                                Discord.setPresence(ClientConfig.NAME.get(), serverCustomMessage.replace("%players%", online + "").replace("%otherpl%", (online - 1) + ""), customIco);
                                usingCustomMsg = true;
                            } else {
                                if (Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")) {
                                    String scoreboardTitle;
                                    try {
                                        scoreboardTitle = removeFormatting(Minecraft.getInstance().world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName().getString());
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
                                    final List<String> scoreboardLines = ScoreboardUtils.getSidebarScores(Minecraft.getInstance().world.getScoreboard());
                                    if (scoreboardTitle.startsWith("SKYBLOCK")) {
                                        for (String line : ScoreboardUtils.getSidebarScores(Minecraft.getInstance().world.getScoreboard())) {
                                            line = removeFormatting(line).trim();
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
                                                final boolean hasBed = (line.contains("\u2713") || line.contains("\u2714"));
                                                final boolean loose = line.contains("\u2717");
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
                                        for (Score s : Minecraft.getInstance().world.getScoreboard().getSortedScores(Minecraft.getInstance().world.getScoreboard().getObjectiveInDisplaySlot(1))) {
                                            if (s.getObjective().equals(Minecraft.getInstance().world.getScoreboard().getObjectiveInDisplaySlot(1))) {
                                                System.out.println(removeFormatting(s.getPlayerName()));
                                                if (removeFormatting(s.getPlayerName()).contains(TextFormatting.getTextWithoutFormattingCodes(Minecraft.getInstance().player.getDisplayName().getString()).substring(0, 12).trim())) {
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
                                    Discord.setPresence(ClientConfig.NAME.get(), "Hypixel [" + scoreboardTitle + "] with " + (online - 1) + " other players", "49tz49873897485");
                                } else if (Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("mineplex.com")) {
                                    Discord.setPresence(ClientConfig.NAME.get(), "Playing on Mineplex with " + (online - 1) + " other players", "23498365347867869");
                                } else if (Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("wynncraft.com")) {
                                    Discord.setPresence(ClientConfig.NAME.get(), "Playing on Wynncraft, The Minecraft MMORPG", "4878hz4389634tz987");
                                } else if (Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("hivemc.com")) {
                                    try {
                                        JsonParser parse = new JsonParser();
                                        URL gameURL = new URL("https://api.hivemc.com/v1/player/" + Minecraft.getInstance().player.getName().getUnformattedComponentText() + "/status/raw?v=1");
                                        HttpURLConnection gameConn = (HttpURLConnection) gameURL.openConnection();
                                        gameConn.setRequestProperty("User-Agent", "ErdbeerbaerLP-DiscordRichPresence-Mod");
                                        InputStream isGame = gameConn.getInputStream();
                                        BufferedReader r = new BufferedReader(new InputStreamReader(isGame, StandardCharsets.UTF_8));
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
                                    } catch (IllegalStateException | MalformedJsonException ignored) {
                                    }
                                } else {
                                    Discord.setPresence(ClientConfig.NAME.get(), ClientConfig.SERVER_MESSAGE.get().replace("%ip%", Minecraft.getInstance().getCurrentServerData().serverIP) + "(" + online + "/" + maxPlayers + " players)", "cube");
                                }
                            }
                            tickAmount = 2000;
                        } else if (tickAmount < 0) tickAmount = 2000;
                        else tickAmount--;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();

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
        return TextFormatting.getTextWithoutFormattingCodes(formatted);
    }

    @SubscribeEvent
    public static void onMenuOpened(GuiOpenEvent event) {
        if (DRPC.isClient && DRPC.isEnabled) {
            if (event.getGui() != null) {
                if ((event.getGui() instanceof MainMenuScreen || event.getGui() instanceof MultiplayerScreen) && !inWorld) {
                    resetVars();
                    checkedUpdate = false;
                    Discord.setPresence(ClientConfig.NAME.get(), ClientConfig.MENU_TEXT.get(), "cube");
                } else if (event.getGui() instanceof DownloadTerrainScreen) {
                    currentOnline = -1;
                    currentMax = -1;
                    if (Minecraft.getInstance().getCurrentServerData() != null) {
                        if (Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")) {
                            resetVars();
                        }
                    }
                }
            }
        }
    }
}
