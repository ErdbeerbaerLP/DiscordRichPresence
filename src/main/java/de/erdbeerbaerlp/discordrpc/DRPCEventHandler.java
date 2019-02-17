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
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.versions.forge.ForgeVersion;

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
	public static void onWorldLeave(WorldEvent.Unload event){

			if(DRPC.isClient && DRPC.isEnabled){
			resetVars();
			Discord.setPresence(ClientConfig.NAME.get(), ClientConfig.MENU_TEXT.get(), "cube");
			}
		
	}
	private static void resetVars(){

			Discord.now = DRPC.gameStarted;
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

			if(DRPC.isClient){
				if(checkedUpdate == false){
					Status result = ForgeVersion.getStatus();
					DRPCLog.Info(result.toString());
					if (result == Status.OUTDATED)
					{
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c Update available!\n\u00A7cCurrent version: \u00A74"+DRPC.VERSION+"\u00A7c, Newest: \u00A7a"+ForgeVersion.getTarget()+"\n\u00A7cChangelog:\n\u00A76"+ForgeVersion.getSpec()).setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://minecraft.curseforge.com/projects/discordrpc")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to get newer Version")))));
						DRPCLog.Fatal("UpdateCheck: Update Available. Download it here: https://minecraft.curseforge.com/projects/discordrichpresence/files");
						checkedUpdate = true;
					}else if(result == Status.AHEAD){
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A77 It looks like you are using an Development version... \n\u00A77Your version: \u00A76"+DRPC.VERSION).setStyle(new Style().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://minecraft.curseforge.com/projects/discordrpc")).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to get current stable Version")))));
						DRPCLog.Fatal("UpdateCheck: Unreleased version... Assuming Development Version");
						checkedUpdate = true;
					}else if(result == Status.FAILED){
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c FAILED TO CHECK FOR UPDATES"));
						DRPCLog.Fatal("UpdateCheck: Unable to check for updates");
						checkedUpdate = true;
					}else if(result == Status.BETA){
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7a You are using an Beta Version. This may contain bugs which are being fixed."));
						DRPCLog.Fatal("UpdateCheck: Beta");
						checkedUpdate = true;
					}else if(result == Status.BETA_OUTDATED){
						event.player.sendMessage(new TextComponentString("\u00A76[\u00A75DiscordRichPresence\u00A76]\u00A7c You are using an Outdated Beta Version. This may contain bugs which are being fixed or are already fixed\n\u00A76Changelog of newer Beta:"+ForgeVersion.getSpec()));
						DRPCLog.Fatal("UpdateCheck: Bata_outdated");
						checkedUpdate = true;
					} else {
						DRPCLog.Info("UpdateCheck: "+result.toString());
						checkedUpdate = true;
					}
				}
                if(DRPC.isEnabled){
			try{
				
				int maxPlayers = -1;
				int online = Minecraft.getInstance().getConnection().getPlayerInfoMap().size();
				if(usingCustomMsg == false && serverCustomMessage.equals("") == false){
					DRPCLog.Debug("CustomMSG Applied");
					Discord.setPresence(ClientConfig.NAME.get(), serverCustomMessage.replace("%players%", online+"").replace("%otherpl%", (online-1)+""), customIco);
					usingCustomMsg = true;
				}
//				System.out.println(Minecraft.getInstance().getCurrentServerData());
				if(Minecraft.getInstance().getCurrentServerData() == null){
					if(tickAmount <= 0){
						World world = Minecraft.getInstance().world;
						IntegratedServer iServer = Minecraft.getInstance().getIntegratedServer();
						EntityPlayerSP player = Minecraft.getInstance().player;
						int posX = Double.valueOf(player.posX).intValue();
						int posY = Double.valueOf(player.posY).intValue();
						int posZ = Double.valueOf(player.posZ).intValue();
						Discord.setPresence(ClientConfig.NAME.get(), ClientConfig.WORLD_MESSAGE.get().replace("%world%", iServer.getFolderName()).replace("%coords%", "X:"+posX+" Y:"+posY+" Z:"+posZ), "world");
						tickAmount = 100;
					}else
					tickAmount--;
				}else{
					if(tickAmount == 0){
						currentMax = maxPlayers;
						currentOnline = online;
			
						if(!serverCustomMessage.isEmpty()){
							DRPCLog.Debug("CustomMSG Applied");
							Discord.setPresence(ClientConfig.NAME.get(), serverCustomMessage.replace("%players%", online+"").replace("%otherpl%", (online-1)+""), customIco);
							usingCustomMsg = true;
						}else{
							if(Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")){
//								Log.Fatal("HYPIXEL!!!!!!!!!");
								String scoreboardTitle = null;
								try {
									scoreboardTitle = Minecraft.getInstance().world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName().getUnformattedComponentText();
								} catch (NullPointerException e) {
									scoreboardTitle = "";
								}
								if(scoreboardTitle.equals("HYPIXEL") || scoreboardTitle.equals("PROTOTYPE") || scoreboardTitle.equals("THE TNT GAMES") || scoreboardTitle.equals("ARCADE GAMES") || scoreboardTitle.equals("CLASSIC GAMES")) scoreboardTitle = "HUB";
								if(scoreboardTitle.equals("")) scoreboardTitle = "AFK";
								if(!scoreboardTitle.equals(Hypixel_LastGame)) Discord.now = Instant.now().getEpochSecond();
								Hypixel_LastGame = scoreboardTitle;
								Discord.setPresence(ClientConfig.NAME.get(), "Hypixel ["+scoreboardTitle+"] with "+(online-1)+" other players", "49tz49873897485");
							}else if(Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("mineplex.com")){
								Discord.setPresence(ClientConfig.NAME.get(), "Playing on Mineplex with "+(online-1)+" other players", "23498365347867869");
							}else if(Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("wynncraft.com")){
								Discord.setPresence(ClientConfig.NAME.get(), "Playing on Wynncraft, The Minecraft MMORPG", "4878hz4389634tz987");		
							}else if(Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("hivemc.com")){
								try {
									JsonParser parse = new JsonParser();
									URL gameURL = new URL("https://api.hivemc.com/v1/player/"+Minecraft.getInstance().player.getName()+"/status/raw?v=1");
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
							Discord.setPresence(ClientConfig.NAME.get(), ClientConfig.SERVER_MESSAGE.get().replace("%ip%", Minecraft.getInstance().getCurrentServerData().serverIP)+"("+online+"/"+maxPlayers+" players)", "cube");
							}
						}
						tickAmount = 2000;
					}else if(tickAmount <0) tickAmount =2000;
					else tickAmount--;
				}
			}catch(Throwable e){
				e.printStackTrace();
				
			}
                }
			}
		
		}
	/**
	 * Removes Color code formatting
	 * @param formatted Formatted text with �2 color codes
	 * @return Raw text without color codes
	 */
	public static String removeFormatting(String formatted){
		return formatted.replaceAll("\u00A70", "").replaceAll("\u00A71", "").replaceAll("\u00A72", "").replaceAll("\u00A73", "").replaceAll("\u00A74", "").replaceAll("\u00A75", "").replaceAll("\u00A76", "").replaceAll("\u00A77", "").replaceAll("\u00A78", "").replaceAll("\u00A79", "").replaceAll("\u00A7a", "").replaceAll("\u00A7b", "").replaceAll("\u00A7c", "").replaceAll("\u00A7d", "").replaceAll("\u00A7e", "").replaceAll("\u00A7f", "").replaceAll("\u00A7l", "").replaceAll("\u00A7k", "").replaceAll("\u00A7m", "").replaceAll("\u00A7n", "").replaceAll("\u00A7o", "").replaceAll("\u00A7r", "");
	}
	@SubscribeEvent
	public static void onMenuOpened(GuiOpenEvent event){
		System.out.println(inWorld);
				if(DRPC.isClient && DRPC.isEnabled){
					String guiName;
					if(event.getGui() != null){
						guiName = event.getGui().toString().split("@")[0];
						DRPCLog.Info("GUI: "+guiName);
					}
				if((event.getGui() instanceof GuiMainMenu || event.getGui() instanceof GuiMultiplayer) && inWorld == false){
					resetVars();
					checkedUpdate = false;
					Discord.setPresence("", "mm", "cube");
				}else if(event.getGui() instanceof GuiDownloadTerrain){
					
			
					DRPC.REQUEST.sendToServer(new RequestMessage("DRPC-Message-Request"));
					currentOnline = -1;
					currentMax = -1;
					if(Minecraft.getInstance().getCurrentServerData() != null){
					if(Minecraft.getInstance().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")){
						resetVars();
					}
					}
				}
			}
		
	}
}
