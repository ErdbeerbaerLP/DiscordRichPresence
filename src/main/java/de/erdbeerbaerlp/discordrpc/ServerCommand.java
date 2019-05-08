package de.erdbeerbaerlp.discordrpc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class ServerCommand implements ICommand {
	private final List aliases;
	public ServerCommand(){
		aliases = new ArrayList();
		aliases.add("discordrichpresencereload");
		aliases.add("drpcreload");
		
	}
	@Override
	public int compareTo(ICommand arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "discordrpcreload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/drpcreload";
	}

	@Override
	public List<String> getAliases() {
		// TODO Auto-generated method stub
		return this.aliases;
	}
	private static final String prefix = "\u00A78[\u00A76DiscordRPC\u00A78] ";
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

			ServerConfig.preInit();
			sender.sendMessage(new TextComponentString(prefix+"\u00A72 Config reloaded!"));
		
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		// TODO Auto-generated method stub
		return sender instanceof MinecraftServer;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		List tabs = new ArrayList();
		
		return tabs;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		// TODO Auto-generated method stub
		return false;
	}

}
