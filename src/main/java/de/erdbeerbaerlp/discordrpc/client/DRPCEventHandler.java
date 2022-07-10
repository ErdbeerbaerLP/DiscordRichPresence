package de.erdbeerbaerlp.discordrpc.client;

import de.erdbeerbaerlp.discordrpc.DRPC;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.scores.Score;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.List;

public class DRPCEventHandler {
    protected static int currentOnline = -1;
    protected static int currentMax = -1;
    protected static boolean inWorld = false;
    protected static String Hypixel_LastGame = "";
    public static String serverCustomMessage = "";
    protected static boolean usingCustomMsg = false;
    protected static boolean checkedUpdate = false;
    public static String customIco = "cube";
    protected static int tickAmount = 120;
    private static int limboTimes = 0;

    @SubscribeEvent
    public static void onWorldLeave(WorldEvent.Unload event) {
        if (DRPC.isClient && DRPC.isEnabled && Minecraft.getInstance().getCurrentServer() == null) {
            resetVars();
            Discord.setPresence(ClientConfig.instance().name, ClientConfig.instance().menuText, "cube");
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

    private static void resetVarsBrief() {
        Discord.now = DRPC.gameStarted;
        Hypixel_LastGame = "-";
        serverCustomMessage = "";
        usingCustomMsg = false;
        tickAmount = 0;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {

        if (DRPC.isClient) {
            if (DRPC.isEnabled) {
                try {

                    int maxPlayers = -1;
                    if (Minecraft.getInstance().getCurrentServer() != null && !Minecraft.getInstance().getCurrentServer().status.getString().isEmpty() && Minecraft.getInstance().getCurrentServer().status.getString().split("/")[1] != null) {
                        maxPlayers = Integer.parseInt(removeFormatting(Minecraft.getInstance().getCurrentServer().status.getString().split("/")[1].trim()));
                    }
                    int online = Minecraft.getInstance().getConnection() == null ? -1 : Minecraft.getInstance().getConnection().getOnlinePlayers().size();

                    if (!usingCustomMsg && !serverCustomMessage.equals("") && ClientConfig.instance().customIntegration) {
                        Discord.setPresence(ClientConfig.instance().name, serverCustomMessage.replace("%players%", online + "").replace("%otherpl%", (online - 1) + ""), customIco);
                        usingCustomMsg = true;
                    }
                    if (Minecraft.getInstance().getCurrentServer() == null) {
                        if (tickAmount <= 0) {
                            IntegratedServer iServer = Minecraft.getInstance().getSingleplayerServer();
                            LocalPlayer player = Minecraft.getInstance().player;
                            int posX = Double.valueOf(player.xOld).intValue();
                            int posY = Double.valueOf(player.yOld).intValue();
                            int posZ = Double.valueOf(player.zOld).intValue();
                            Discord.setPresence(ClientConfig.instance().name, ClientConfig.instance().worldMessage.replace("%world%", iServer.getWorldData().getLevelName()).replace("%coords%", "X:" + posX + " Y:" + posY + " Z:" + posZ), "world");
                            tickAmount = 100;
                        } else
                            tickAmount--;
                    } else {
                        if (tickAmount == 0) {
                            currentMax = maxPlayers;
                            currentOnline = online;
                            if (!serverCustomMessage.isEmpty()) {
                                Discord.setPresence(ClientConfig.instance().name, serverCustomMessage.replace("%players%", online + "").replace("%otherpl%", (online - 1) + ""), customIco);
                                usingCustomMsg = true;
                            } else {
                                final String serverAddress = Minecraft.getInstance().getCurrentServer().ip.toLowerCase();
                                if (serverAddress.contains("hypixel.net") && ClientConfig.instance().hypixelIntegration) {
                                    String scoreboardTitle;
                                    try {
                                        scoreboardTitle = removeFormatting(Minecraft.getInstance().level.getScoreboard().getDisplayObjective(1).getDisplayName().getString());
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
                                        Discord.now = Instant.now();
                                    Hypixel_LastGame = scoreboardTitle;
                                    final List<String> scoreboardLines = ScoreboardUtils.getSidebarScores(Minecraft.getInstance().level.getScoreboard());
                                    if (scoreboardTitle.startsWith("SKYBLOCK")) {
                                        for (String line : ScoreboardUtils.getSidebarScores(Minecraft.getInstance().level.getScoreboard())) {
                                            line = removeFormatting(line).trim();
                                            if (removeFormatting(line).startsWith("\u23E3")) {
                                                line = line.replace("\u23E3", "").trim();
                                                if (line.equalsIgnoreCase("none"))
                                                    Discord.setPresence("Hypixel", "Skyblock - Unknown place", "49tz49873897485");
                                                else if (line.equalsIgnoreCase("Your island"))
                                                    Discord.setPresence("Hypixel", "Skyblock - Private Island", "49tz49873897485");
                                                else
                                                    Discord.setPresence("Hypixel", "Skyblock - " + line, "49tz49873897485");
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
                                            DRPC.LOGGER.error("Unknown State!!!");
                                            for (final String l : scoreboardLines) {
                                                DRPC.LOGGER.error(removeFormatting(l));
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
                                        for (Score s : Minecraft.getInstance().level.getScoreboard().getPlayerScores(Minecraft.getInstance().level.getScoreboard().getDisplayObjective(1))) {
                                            if (s.getObjective().equals(Minecraft.getInstance().level.getScoreboard().getDisplayObjective(1))) {
                                                System.out.println(removeFormatting(s.getOwner()));
                                                if (removeFormatting(s.getOwner()).contains(ChatFormatting.stripFormatting(Minecraft.getInstance().player.getDisplayName().getString()).substring(0, 12).trim())) {
                                                    Discord.setPresence("Hypixel", "Turbo Kart Racers - In Game - Position: " + removeFormatting(s.getOwner()).charAt(0), "49tz49873897485");
                                                    return;
                                                }
                                                if (removeFormatting(s.getOwner()).contains("You're")) {
                                                    Discord.setPresence("Hypixel", "Turbo Kart Racers - Finished - Position: " + removeFormatting(s.getOwner()).replace("You're #", "").trim(), "49tz49873897485");
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
                                    Discord.setPresence(ClientConfig.instance().name, "Hypixel [" + scoreboardTitle + "] with " + (online - 1) + " other players", "49tz49873897485");
                                } else if (serverAddress.contains("mineplex.com") && ClientConfig.instance().customIntegration) {
                                    Discord.setPresence(ClientConfig.instance().name, "Playing on Mineplex with " + (online - 1) + " other players", "23498365347867869");
                                } else if (serverAddress.contains("wynncraft.com") && ClientConfig.instance().customIntegration) {
                                    Discord.setPresence(ClientConfig.instance().name, "Playing on Wynncraft, The Minecraft MMORPG", "4878hz4389634tz987");
                                } else {
                                    Discord.setPresence(ClientConfig.instance().name, ClientConfig.instance().serverMessage.replace("%ip%", serverAddress) + "(" + online + "/" + maxPlayers + " players)", "cube");
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
        return ChatFormatting.stripFormatting(formatted);
    }

    @SubscribeEvent
    public static void onMenuOpened(ScreenEvent.Opening event) {
        if (DRPC.isClient && DRPC.isEnabled) {
            event.getScreen();
            if ((event.getScreen() instanceof TitleScreen || event.getScreen() instanceof JoinMultiplayerScreen) && !inWorld) {
                resetVars();
                checkedUpdate = false;
                Discord.setPresence(ClientConfig.instance().name, ClientConfig.instance().menuText, "cube");
            } else if (event.getScreen() instanceof LevelLoadingScreen) {
                currentOnline = -1;
                currentMax = -1;
                if (Minecraft.getInstance().getCurrentServer() != null) {
                    if (Minecraft.getInstance().getCurrentServer().ip.toLowerCase().contains("hypixel.net")) {
                        resetVarsBrief();
                    }
                }
            }
        }
    }

}
