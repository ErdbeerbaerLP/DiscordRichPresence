package de.erdbeerbaerlp.discordrpc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

public class Command {

	public static class CommandHelp{
		static ArgumentBuilder<CommandSource, ?> register()
		{
			return null;
			
		}
		private static TextComponentString helpMsg = new TextComponentString(DRPC.COMMAND_MESSAGE_PREFIX
				+"\u00A72 Help:\n"
				+ "\u00A76/drpc help \u00A77   Displays this\n"
				+ "\u00A76/drpc reload \u00A77  Reloads the config file\n"
				+ "\u00A76/drpc dev \u00A77   Some debugging functions");
		
		private static int execute(CommandContext<CommandSource> ctx) {
			// TODO Auto-generated method stub

			try {
				ctx.getSource().asPlayer().sendMessage(helpMsg);
			} catch (CommandSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
			return 1;
		}
	}
	public static class CommandReload{}
	public static class CommandDev{

		public static ArgumentBuilder<CommandSource, ?> register() {
			// TODO Auto-generated method stub
			return null;
		}


	}
	public Command(CommandDispatcher<CommandSource> dispatcher)
    {
	dispatcher.register(
            LiteralArgumentBuilder.<CommandSource>literal("drpc")
            .then(CommandHelp.register())
            .then(CommandDev.register())
            );
    }
	


}
