package de.erdbeerbaerlp.discordrpc;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.IClientCommand;

import java.util.ArrayList;
import java.util.List;

public class Command implements IClientCommand {
    /**
     * Chat message prefix
     */
    public static final String prefix = "\u00A78[\u00A76DiscordRPC\u00A78] ";
    private final List<String> aliases;
    private final String[] tabcompletions = new String[]{"help", "reload", "dev"};
    private final String[] devtabs = new String[]{"logtochat", "msg-request"};

    public Command() {
        aliases = new ArrayList<String>();
        aliases.add("discordrichpresence");
        aliases.add("drpc");

    }

    @Override
    public int compareTo(ICommand arg0) {
        return 0;
    }

    @Override
    public String getName() {
        return "discordrpc";
    }

    @Override
    public String getUsage(ICommandSender sender) {

        return "/drpc <help|reload|dev>";
    }

    @Override
    public List<String> getAliases() {

        return this.aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        TextComponentString helpMsg = new TextComponentString(prefix
                + "\u00A72 Help:\n"
                + "\u00A76/drpc help \u00A77   Displays this\n"
                + "\u00A76/drpc reload \u00A77  Reloads the config file\n"
                + "\u00A76/drpc dev \u00A77   Some debugging functions");

        if (args.length > 0) {
            switch (args[0]) {
                case "help":
                    sender.sendMessage(helpMsg);

                    break;
                case "reload":
                    RPCconfig.loadConfigFromFile();
                    sender.sendMessage(new TextComponentString(prefix + "\u00A72 Config reloaded!"));
                    break;
                case "dev":
                    if (args.length > 1 && RPCconfig.DEV_COMMANDS) {
                        switch (args[1]) {
                            case "logtochat":
                                if (ModClass.logtochat == false) {
                                    sender.sendMessage(new TextComponentString(prefix + "\u00A7aTurned on log in chat!"));
                                    ModClass.logtochat = true;
                                } else {
                                    sender.sendMessage(new TextComponentString(prefix + "\u00A7aTurned off log in chat!"));
                                    ModClass.logtochat = false;
                                }
                                DRPCLog.Debug("Test");
                                DRPCLog.Info("Test");
                                DRPCLog.Error("Test");
                                DRPCLog.Fatal("Test");
                                break;
                            case "msg-request":
                                ModClass.REQUEST.sendToServer(new RequestMessage("DRPC-Message-Request"));
                                sender.sendMessage(new TextComponentString(prefix + "\u00A76Sent message request to Server!"));
                                break;
                            default:
                                sender.sendMessage(new TextComponentString(prefix + "\u00A7cInvalid argument!"));
                                break;
                        }
                    } else if (!(args.length > 1) && RPCconfig.DEV_COMMANDS)
                        sender.sendMessage(new TextComponentString(prefix + "\u00A7cNot enough arguments for argument \"dev\""));
                    else if (!RPCconfig.DEV_COMMANDS)
                        sender.sendMessage(new TextComponentString(prefix + "\u00A7cDevelopment commands are disabled in the config!"));
                    break;
                default:
                    sender.sendMessage(new TextComponentString(prefix + "\u00A7cInvalid argument!"));
                    break;
            }
        } else {
            sender.sendMessage(new TextComponentString(prefix + "\u00A7cNot enough arguments!"));
            sender.sendMessage(helpMsg);
        }

    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {

        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          BlockPos targetPos) {
        List<String> tabs = new ArrayList<String>();
        if (args.length == 1) {

            for (String tabcompletion : tabcompletions) {
                if (tabcompletion.startsWith(args[0]))
                    tabs.add(tabcompletion);
            }
        } else if (args.length == 2) {
            if (args[0].equals("dev") && RPCconfig.DEV_COMMANDS) {
                for (String devtab : devtabs) {
                    if (devtab.startsWith(args[1]))
                        tabs.add(devtab);
                }
            }
        }
        return tabs;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {

        return false;
    }

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {

        return false;
    }

}
