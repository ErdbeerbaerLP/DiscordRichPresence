package de.erdbeerbaerlp.discordrpc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class Command {
	
	private static class CommandHelp
	{
		static ArgumentBuilder<CommandSource, ?> register() {
			return Commands.literal("help").requires(cs -> cs.hasPermissionLevel(0)).executes((ctx) -> execute(ctx));
			
		}
		
		private static StringTextComponent helpMsg = new StringTextComponent(
				DRPC.COMMAND_MESSAGE_PREFIX + "\u00A72 Help:\n" + "\u00A76/drpc help \u00A77   Displays this\n" + "\u00A76/drpc reload \u00A77  Reloads the config file\n" + "\u00A76/drpc dev \u00A77   Some debugging functions");
		
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
	private static class CommandReload{}
	private static class CommandDev{
		private static class CommandLogToChat{
			static ArgumentBuilder<CommandSource, ?> register()
			{
				return Commands.literal("logtochat")
						.requires(cs->cs.hasPermissionLevel(0))
						.executes((ctx)->execute(ctx));

			}

			private static int execute(CommandContext<CommandSource> ctx) {
				try {
					final ServerPlayerEntity sender = ctx.getSource().asPlayer();
					if (DRPC.logtochat == false) {
						sender.sendMessage(new StringTextComponent(DRPC.COMMAND_MESSAGE_PREFIX + "\u00A7aTurned on log in chat!"));
						DRPC.logtochat = true;
					}
					else {
						sender.sendMessage(new StringTextComponent(DRPC.COMMAND_MESSAGE_PREFIX + "\u00A7aTurned off log in chat!"));
						DRPC.logtochat = false;
					}
					DRPCLog.Debug("Test");
					DRPCLog.Info("Test");
					DRPCLog.Error("Test");
					DRPCLog.Fatal("Test");
					return 1;
				} catch (CommandSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return 0;
				}
				
			}
		}
		
		public static ArgumentBuilder<CommandSource, ?> register() {
			// TODO Auto-generated method stub
			return ClientConfig.DEV_COMMANDS.get()?Commands.literal("dev")
					.requires(cs->cs.hasPermissionLevel(0))
					.executes((ctx)->CommandHelp.execute(ctx))
					.then(CommandLogToChat.register())
					
					:null;
		}

	}
	
	
	
	public Command(CommandDispatcher<CommandSource> dispatcher)
	{
		if(ClientConfig.DEV_COMMANDS.get())
			dispatcher.register(
					LiteralArgumentBuilder.<CommandSource>literal("drpc")
					.then(CommandHelp.register())
					.then(CommandDev.register())
					.executes((ctx)->CommandHelp.execute(ctx))
					);
		else
			dispatcher.register(
					LiteralArgumentBuilder.<CommandSource>literal("drpc")
					.then(CommandHelp.register())
					.executes((ctx)->CommandHelp.execute(ctx))
					);

	}



}
