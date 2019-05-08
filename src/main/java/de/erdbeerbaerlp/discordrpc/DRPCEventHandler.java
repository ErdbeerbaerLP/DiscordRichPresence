package de.erdbeerbaerlp.discordrpc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;

import fr.nukerhd.hiveapi.response.games.Games;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
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

public class DRPCEventHandler {
	protected static int currentOnline = -1;
	protected static int currentMax = -1;
	protected static boolean inWorld = false;
	protected static String Hypixel_LastGame = "";
	protected static String serverCustomMessage = "";
	protected static boolean usingCustomMsg = false;
	protected static boolean checkedUpdate = false;

	protected static String customIco = "cube";
	
	@SubscribeEvent
	public static void onConfigurationChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
		if (e.getModID().equalsIgnoreCase(ModClass.MODID)) {
			// Resync configs
			DRPCLog.Info("Configuration has changed");
			RPCconfig.reloadConfig();
		}
	}
	
	
	@SubscribeEvent
	public static void onWorldLeave(ClientDisconnectionFromServerEvent event){

			if(ModClass.isClient && ModClass.isEnabled){
			resetVars();
			Discord.setPresence(RPCconfig.NAME, "In Main Menu", "cube");
			}
		
	}
	private static void resetVars(){

			Discord.now = ModClass.gameStarted;
			inWorld = false;
			currentOnline = -1;
			currentMax = -1;
			Hypixel_LastGame = "-";
			serverCustomMessage = "";
			usingCustomMsg = false;
			tickAmount = 0;
			customIco="cube"; 
	
	}
	
	protected static int tickAmount = 120;
	@SubscribeEvent
	public static void onTick(PlayerTickEvent event){

			if(ModClass.isClient){
				if(checkedUpdate == false){
					CheckResult result = ForgeVersion.getResult(Loader.instance().getIndexedModList().get(ModClass.MODID));
					if (result.status == Status.OUTDATED)
					{
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c Update available!\n\u00A7cCurrent version: \u00A74"+ModClass.VERSION+"\u00A7c, Newest: \u00A7a"+result.target+"\n\u00A7cChangelog:\n\u00A76"+result.changes.get(result.target)).setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, result.url)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to get newer Version")))));
						DRPCLog.Fatal("UpdateCheck: Update Available. Download it here: https://minecraft.curseforge.com/projects/discordrichpresence/files");
						checkedUpdate = true;
					}else if(result.status == Status.AHEAD){
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A77 It looks like you are using an Development version... \n\u00A77Your version: \u00A76"+ModClass.VERSION).setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, result.url)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to get current stable Version")))));
						DRPCLog.Fatal("UpdateCheck: Unreleased version... Assuming Development Version");
						checkedUpdate = true;
					}else if(result.status == Status.FAILED){
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c FAILED TO CHECK FOR UPDATES"));
						DRPCLog.Fatal("UpdateCheck: Unable to check for updates");
						checkedUpdate = true;
					}else if(result.status == Status.BETA){
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7a You are using an Beta Version. This may contain bugs which are being fixed."));
						DRPCLog.Fatal("UpdateCheck: Beta");
						checkedUpdate = true;
					}else if(result.status == Status.BETA_OUTDATED){
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c You are using an Outdated Beta Version. This may contain bugs which are being fixed or are already fixed\n\u00A76Changelog of newer Beta:"+result.changes.get(result.target)));
						DRPCLog.Fatal("UpdateCheck: Bata_outdated");
						checkedUpdate = true;
					} else {
						DRPCLog.Info("UpdateCheck: "+result.status.toString());
						checkedUpdate = true;
					}
				}
                if(ModClass.isEnabled){
			try{
				
				int maxPlayers = Minecraft.getMinecraft().getConnection().currentServerMaxPlayers;
				int online = Minecraft.getMinecraft().getConnection().getPlayerInfoMap().size();
				if(usingCustomMsg == false && serverCustomMessage.equals("") == false){
					DRPCLog.Debug("CustomMSG Applied");
					Discord.setPresence(RPCconfig.NAME, serverCustomMessage.replace("%players%", online+"").replace("%otherpl%", (online-1)+""), customIco);
					usingCustomMsg = true;
				}
//				System.out.println(Minecraft.getMinecraft().getCurrentServerData());
				if(Minecraft.getMinecraft().getCurrentServerData() == null){
					if(tickAmount <= 0){
						World world = Minecraft.getMinecraft().world;
						IntegratedServer iServer = Minecraft.getMinecraft().getIntegratedServer();
						EntityPlayerSP player = Minecraft.getMinecraft().player;
						int posX = Double.valueOf(player.posX).intValue();
						int posY = Double.valueOf(player.posY).intValue();
						int posZ = Double.valueOf(player.posZ).intValue();
						Discord.setPresence(RPCconfig.NAME, RPCconfig.WORLD_MESSAGE.replace("%world%", iServer.getFolderName()).replace("%coords%", "X:"+posX+" Y:"+posY+" Z:"+posZ), "world");
						tickAmount = 100;
					}else
					tickAmount--;
				}else{
					if(tickAmount == 0){
						currentMax = maxPlayers;
						currentOnline = online;
						Discord.enableJoinRequest();
						if(!serverCustomMessage.isEmpty()){
							DRPCLog.Debug("CustomMSG Applied");
							Discord.setPresence(RPCconfig.NAME, serverCustomMessage.replace("%players%", online+"").replace("%otherpl%", (online-1)+""), customIco);
							usingCustomMsg = true;
						}else{
							if(Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")){
								String scoreboardTitle = null;
								try {
									scoreboardTitle = removeFormatting(Minecraft.getMinecraft().world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName());
								} catch (NullPointerException e) {
									scoreboardTitle = "";
								}
								if(scoreboardTitle.equals("HYPIXEL") || scoreboardTitle.equals("PROTOTYPE") || scoreboardTitle.equals("THE TNT GAMES") || scoreboardTitle.equals("ARCADE GAMES") || scoreboardTitle.equals("CLASSIC GAMES")) scoreboardTitle = "HUB";
								if(scoreboardTitle.equals("")) scoreboardTitle = "AFK";
								if(!scoreboardTitle.equals(Hypixel_LastGame)) Discord.now = Instant.now().getEpochSecond();
								Hypixel_LastGame = scoreboardTitle;
								Discord.setPresence(RPCconfig.NAME, "Hypixel ["+scoreboardTitle+"] with "+(online-1)+" other players", "49tz49873897485");
							}else if(Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("mineplex.com")){
								Discord.setPresence(RPCconfig.NAME, "Playing on Mineplex with "+(online-1)+" other players", "23498365347867869");
							}else if(Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("wynncraft.com")){
								Discord.setPresence(RPCconfig.NAME, "Playing on Wynncraft, The Minecraft MMORPG", "4878hz4389634tz987");		
							}else if(Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hivemc.com")){
								try {
									JsonParser parse = new JsonParser();
									URL gameURL = new URL("https://api.hivemc.com/v1/player/"+Minecraft.getMinecraft().player.getName()+"/status/raw?v=1");
									HttpURLConnection gameConn = (HttpURLConnection) gameURL.openConnection();
									gameConn.setRequestProperty("User-Agent", "ErdbeerbaerLP-DiscordRichPresence-Mod");
									InputStream isGame = gameConn.getInputStream();
									BufferedReader r = new BufferedReader(new InputStreamReader(isGame, Charset.forName("UTF-8")));
									String result = r.lines().collect(Collectors.joining());
									JsonElement json = parse.parse(result);
									gameConn.disconnect();
									String gameS = json.getAsJsonObject().get("status").getAsString();
									String gameO;
									try{
									gameO = Games.valueOf(gameS).getName();
									}catch (IllegalArgumentException e){
										gameO = gameS;
									}
									if(gameO.equals("HUB")) gameO = "in HUB";
									if(gameO.equals("BEDT")) gameO = "BedWars";
									  Discord.setPresence("TheHive", "Playing "+gameO+" on hivemc.com", "38462896734683686");
								} catch (IllegalStateException | MalformedJsonException e) {
								}
									
								
							      
							}else{
							Discord.setPresence(RPCconfig.NAME, RPCconfig.SERVER_MESSAGE.replace("%ip%", Minecraft.getMinecraft().getCurrentServerData().serverIP)+"("+online+"/"+maxPlayers+" players)", "cube");
							}
						}
						tickAmount = 2000;
					}else if(tickAmount <0) tickAmount =2000;
					else tickAmount--;
				}
			}catch(Throwable e){
				DRPCLog.Error("(Usually not a bug) ERROR in onPlayerTick... "+e);
				
			}
                }
			}
		
		}
	/**
	 * Removes Color code formatting
	 * @param formatted Formatted text with §2 color codes
	 * @return Raw text without color codes
	 */
	public static String removeFormatting(String formatted){
		return formatted.replaceAll("\u00A70", "").replaceAll("\u00A71", "").replaceAll("\u00A72", "").replaceAll("\u00A73", "").replaceAll("\u00A74", "").replaceAll("\u00A75", "").replaceAll("\u00A76", "").replaceAll("\u00A77", "").replaceAll("\u00A78", "").replaceAll("\u00A79", "").replaceAll("\u00A7a", "").replaceAll("\u00A7b", "").replaceAll("\u00A7c", "").replaceAll("\u00A7d", "").replaceAll("\u00A7e", "").replaceAll("\u00A7f", "").replaceAll("\u00A7l", "").replaceAll("\u00A7k", "").replaceAll("\u00A7m", "").replaceAll("\u00A7n", "").replaceAll("\u00A7o", "").replaceAll("\u00A7r", "");
	}
	@SubscribeEvent
	public static void onMenuOpened(GuiOpenEvent event){

				if(ModClass.isClient && ModClass.isEnabled){
					String guiName;
					if(event.getGui() == null){
					}
					else{ 
						guiName = event.getGui().toString().split("@")[0];
						DRPCLog.Debug("GUI: "+guiName);
					}
				if((event.getGui() instanceof GuiMainMenu || event.getGui() instanceof GuiMultiplayer) && inWorld == false){
					resetVars();
					checkedUpdate = false;
					Discord.setPresence(RPCconfig.NAME, "In Main Menu", "cube");
					//new RPCCrash("MANUAL INITIATED CRASH", new NullPointerException("TEST"));
				}else if(event.getGui() instanceof GuiDownloadTerrain){
					
					ModClass.REQUEST.sendToServer(new RequestMessage("DRPC-Message-Request"));
					currentOnline = -1;
					currentMax = -1;
					if(Minecraft.getMinecraft().getCurrentServerData() != null){
					if(Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")){
						resetVars();
					}
					}
				}
			}
		
	}
}
