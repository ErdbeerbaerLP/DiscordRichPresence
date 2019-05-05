package de.erdbeerbaerlp.discordrpc;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.IClientCommand;

public class Command implements IClientCommand {
	private final List aliases;
	private final String[] tabcompletions= new String[]{"help","reload", "dev"};
	private final String[] devtabs = new String[]{"logtochat", "msg-request"};
	public Command(){
		aliases = new ArrayList();
		aliases.add("discordrichpresence");
		aliases.add("drpc");
		
	}
	@Override
	public int compareTo(ICommand arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "discordrpc";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/drpc <help|reload|dev>";
	}

	@Override
	public List<String> getAliases() {
		// TODO Auto-generated method stub
		return this.aliases;
	}
	/**
	 * Chat message prefix
	 */
	public static final String prefix = "\u00A78[\u00A76DiscordRPC\u00A78] ";
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		// TODO Auto-generated method stub
		TextComponentString helpMsg = new TextComponentString(prefix
				+"\u00A72 Help:\n"
				+ "\u00A76/drpc help \u00A77   Displays this\n"
				+ "\u00A76/drpc reload \u00A77  Reloads the config file\n"
				+ "\u00A76/drpc dev \u00A77   Some debugging functions");
		
		if(args.length > 0){
		switch (args[0]){
		case "help":
			sender.sendMessage(helpMsg);
			
			break;
		case "reload":
			RPCconfig.loadConfigFromFile();
			sender.sendMessage(new TextComponentString(prefix+"\u00A72 Config reloaded!"));
			break;
		case "dev":
			if(args.length > 1 && RPCconfig.DEV_COMMANDS){
				switch (args[1]){
				case "logtochat":
					if(ModClass.logtochat == false){
						sender.sendMessage(new TextComponentString(prefix+"\u00A7aTurned on log in chat!"));
						ModClass.logtochat = true;
					}else{
						sender.sendMessage(new TextComponentString(prefix+"\u00A7aTurned off log in chat!"));
						ModClass.logtochat = false;
					}
					DRPCLog.Debug("Test");
					DRPCLog.Info("Test");
					DRPCLog.Error("Test");
					DRPCLog.Fatal("Test");
					break;
				case "msg-request":
					ModClass.REQUEST.sendToServer(new RequestMessage("DRPC-Message-Request"));
					sender.sendMessage(new TextComponentString(prefix+"\u00A76Sent message request to Server!"));
					break;
				default:
					sender.sendMessage(new TextComponentString(prefix+"\u00A7cInvalid argument!"));
					break;
				}	
			}else if(!(args.length > 1 ) && RPCconfig.DEV_COMMANDS) sender.sendMessage(new TextComponentString(prefix+"\u00A7cNot enough arguments for argument \"dev\""));
			else if(!RPCconfig.DEV_COMMANDS) sender.sendMessage(new TextComponentString(prefix+"\u00A7cDevelopment commands are disabled in the config!"));
			break;
		default:
			sender.sendMessage(new TextComponentString(prefix+"\u00A7cInvalid argument!"));
			break;
		}
		}else {
			sender.sendMessage(new TextComponentString(prefix+"\u00A7cNot enough arguments!"));
			sender.sendMessage(helpMsg);
		}
		
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		List tabs = new ArrayList();
		if(args.length == 1){
		
			for(int i = 0; i < tabcompletions.length;i++){
				if(tabcompletions[i].startsWith(args[0]))
					tabs.add(tabcompletions[i]);
			}
		}else if(args.length == 2){
			if(args[0].equals("dev") && RPCconfig.DEV_COMMANDS){
				for(int i = 0; i < devtabs.length;i++){
					if(devtabs[i].startsWith(args[1]))
						tabs.add(devtabs[i]);
				}
			}
		}
		return tabs;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
		// TODO Auto-generated method stub
		return false;
	}

}
