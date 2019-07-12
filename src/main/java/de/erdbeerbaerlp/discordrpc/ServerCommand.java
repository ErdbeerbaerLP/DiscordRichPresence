package de.erdbeerbaerlp.discordrpc;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public class ServerCommand implements ICommand {
    private static final String prefix = "\u00A78[\u00A76DiscordRPC\u00A78] ";
    private final List aliases;

    @SuppressWarnings("unchecked")
    public ServerCommand() {
        aliases = new ArrayList();
        aliases.add("discordrichpresencereload");
        aliases.add("drpcreload");

    }

    @Override
    public int compareTo(ICommand arg0) {
        return 0;
    }

    @Override
    public String getName() {
        return "discordrpcreload";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/drpcreload";
    }

    @Override
    public List<String> getAliases() {
        return this.aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {

        ServerConfig.preInit();
        sender.sendMessage(new TextComponentString(prefix + "\u00A72 Config reloaded!"));

    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof MinecraftServer;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          BlockPos targetPos) {

        return (List) new ArrayList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

}
