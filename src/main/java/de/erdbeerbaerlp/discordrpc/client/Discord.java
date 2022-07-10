package de.erdbeerbaerlp.discordrpc.client;

import de.erdbeerbaerlp.discordrpc.DRPC;
import de.jcm.discordgamesdk.*;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.lobby.Lobby;
import de.jcm.discordgamesdk.lobby.LobbyTransaction;
import de.jcm.discordgamesdk.lobby.LobbyType;
import de.jcm.discordgamesdk.user.DiscordUser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Discord {
    private static final CreateParams p = new CreateParams();
    private static final Activity presence = new Activity();
    private static final boolean isDev = false;
    private static final DiscordThread t = new DiscordThread();
    public static Instant now = DRPC.gameStarted;
    private static Core core;
    private static String currentTitle;
    private static String currentSubtitle;
    private static String currentImgKey;
    private static boolean initialized = false;
    private static String currentJoinRequestKey = "";
    private static long currentLobbyID = -1;

    /**
     * Disables all calls from this mod allowing to set custom data from another mod (Still loads configs)
     *
     * @param preventConfigLoad true to prevent loading the config file
     */
    public static void disableModDefault(boolean preventConfigLoad) {
        DRPC.isEnabled = false;
        DRPC.preventConfigLoad = preventConfigLoad;
    }

    /**
     * Disables all calls from this Mod allowing to set custom data from another mod (Still loads configs)
     */
    public static void disableModDefault() {
        disableModDefault(false);
    }

    public static List<String> getPlayersInLobby(long lobbyid) {

        final LobbyManager lobbyManager = core.lobbyManager();
        final List<DiscordUser> memberUsers = lobbyManager.getMemberUsers(lobbyid);

        final List<String> out = new ArrayList<>();
        memberUsers.forEach((m) -> {
            out.add(m.getUsername() + "#" + m.getDiscriminator());
        });
        return out;
    }

    public static void createLobby(String server, String mcname, int capacity) {
        final LobbyManager lobbyManager = core.lobbyManager();
        final LobbyTransaction transaction = lobbyManager.getLobbyCreateTransaction();
        transaction.setType(LobbyType.PRIVATE);
        transaction.setCapacity(capacity);
        transaction.setMetadata("server", server);
        transaction.setMetadata("mcname", mcname);
        lobbyManager.createLobby(transaction, (l) -> {
            currentJoinRequestKey = core.lobbyManager().getLobbyActivitySecret(l);
            currentLobbyID = l.getId();
        });
    }

    public static boolean isLobbyOwner(long lobby) {
        final LobbyManager lobbyManager = core.lobbyManager();
        final Lobby lobby1 = lobbyManager.getLobby(lobby);
        return lobby1.getOwnerId() == core.userManager().getCurrentUser().getUserId();
    }

    public static void leaveLobby(long id) {
        final LobbyManager lobbyManager = core.lobbyManager();
        lobbyManager.disconnectLobby(id);
        currentJoinRequestKey = "";
        currentLobbyID = -1;
    }

    public static void deleteLobby(long id) {
        final LobbyManager lobbyManager = core.lobbyManager();
        lobbyManager.deleteLobby(id);
        currentJoinRequestKey = "";
        currentLobbyID = -1;
    }

    public static void joinLobby(String secret) {
        final LobbyManager lobbyManager = core.lobbyManager();
        lobbyManager.connectLobbyWithActivitySecret(secret, (res, lobby) -> {
            if (res == Result.OK) {
                currentLobbyID = lobby.getId();
                currentJoinRequestKey = core.lobbyManager().getLobbyActivitySecret(lobby);
            }
        });

    }

    /**
     * Starts up the discord rich presence service (call in Constructor or earlier!)
     */
    public static void initDiscord() {
        if (initialized) return;
        p.setClientID(Long.parseLong(ClientConfig.instance().clientID));
        p.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD);
        p.registerEventHandler(new DCEventHandler());
        core = new Core(p);
        DRPC.LOGGER.info("Starting Discord");
        if (!t.isAlive()) t.start();
        Discord.initialized = true;
    }

    /**
     * Same as {@link #initDiscord initDiscord} but allowing you to set an custom Client ID
     *
     * @param clientID <a href="https://discordpy.readthedocs.io/en/rewrite/discord.html"> See here</a>
     */
    public static void customDiscordInit(String clientID) {
        if (initialized) return;
        p.setClientID(Long.parseLong(clientID));
        p.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD);
        core = new Core(p);
        DRPC.LOGGER.info("Starting Discord with client ID " + clientID);
        if (!t.isAlive()) t.start();
        Discord.initialized = true;
    }

    public static String getCurrentJoinRequestKey() {
        return currentJoinRequestKey;
    }

    public static long getCurrentLobbyID() {
        return currentLobbyID;
    }

    public static void setPresence(String title, String subtitle, String iconKey, boolean useUUID) {
        presence.setDetails(title);
        currentTitle = title;
        presence.setState(subtitle);
        currentSubtitle = subtitle;
        presence.assets().setLargeImage(iconKey);
        currentImgKey = iconKey;
        presence.timestamps().setStart(now);

        if (currentLobbyID != -1) {
            final Lobby lobby = core.lobbyManager().getLobby(currentLobbyID);
            presence.party().setID(lobby.getId() + "");
            presence.party().size().setMaxSize(lobby.getCapacity());
            presence.party().size().setCurrentSize(core.lobbyManager().memberCount(lobby));
        }

        presence.secrets().setJoinSecret(currentJoinRequestKey);
        presence.party().setID(currentLobbyID + "");
        core.activityManager().updateActivity(presence);

    }

    /**
     * Sets the DiscordRichPresence
     *
     * @param title    The first line of the RichPresence (Below "Minecraft")
     * @param subtitle The second line of RichPresence
     * @param iconKey  The icon key.<br> <B>Default Icon Keys:</b> <br>world,<br> cube,<br> 34565655649643693 (Black and white cube),<br> 3454083453475893469 (Half color, Half B{@literal &}W cube)
     */
    public static void setPresence(String title, String subtitle, String iconKey) {
        setPresence(title, subtitle, iconKey, true);
    }

    protected static void reloadPresence() {
        setPresence(ClientConfig.instance().name, currentSubtitle, currentImgKey);
    }

    protected static String getTitle() {
        return currentTitle;
    }

    protected static String getSubtitle() {
        return currentSubtitle;
    }

    protected static String getImgKey() {
        return currentImgKey;
    }

    protected static boolean isPlayerDev() {
        return isDev;
    }

    protected static Activity getPresence() {
        return presence;
    }

    public static void shutdown() {
        core.close();
    }

    public static boolean openInvite(String url) {
        if (core.overlayManager().isEnabled()) {
            core.overlayManager().openGuildInvite(url);
            return true;
        }
        return false;
    }

    static class DCEventHandler extends DiscordEventAdapter {
        @Override
        public void onActivityJoin(String secret) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Join key: " + secret));
            joinLobby(secret);
        }

        @Override
        public void onActivityJoinRequest(DiscordUser user) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Received join-request from " + user.getDiscriminator()));
        }

        public void onMemberConnect(long lobbyId, long userId) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(lobbyId + ""));
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("" + userId));

        }


        @Override
        public void onLobbyDelete(long lobbyId, int reason) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(lobbyId + ""));
            super.onLobbyDelete(lobbyId, reason);
        }

        @Override
        public void onMemberUpdate(long lobbyId, long userId) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(lobbyId + ""));
            super.onMemberUpdate(lobbyId, userId);
        }

        @Override
        public void onMemberDisconnect(long lobbyId, long userId) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(lobbyId + ""));
            super.onMemberDisconnect(lobbyId, userId);
        }

        @Override
        public void onLobbyMessage(long lobbyId, long userId, byte[] data) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(lobbyId + ""));
            super.onLobbyMessage(lobbyId, userId, data);
        }

        public void onLobbyUpdate(long lobbyId) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(lobbyId + ""));
        }
    }

    public static class DiscordThread extends Thread {
        @Override
        public void run() {
            while (true) {
                core.runCallbacks();
                try {
                    // Sleep a bit to save CPU
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
