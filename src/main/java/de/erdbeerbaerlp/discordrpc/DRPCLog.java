package de.erdbeerbaerlp.discordrpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class DRPCLog {
	private static Logger Log = LogManager.getLogger(DRPC.MODID);

	public static void Debug(String msg) {

		Log.debug(msg);
		if (DRPC.logtochat) {
			try {
				Minecraft.getInstance().player.sendMessage(new TextComponentString("\u00A77\u00A7o[DEBUG] " + msg));
			} catch (Exception e) {
			}
		}
	}

	public static void Info(String msg) {

		Log.info(msg);
		if (DRPC.logtochat) {
			try {
				Minecraft.getInstance().player
						.sendMessage(new TextComponentString("\u00A77\u00A7o[Info]\u00A7f\u00A7o " + msg));
			} catch (Exception e) {
			}
		}
	}

	public static void Error(String msg) {

		Log.error(msg);
		if (DRPC.logtochat) {
			try {
				Minecraft.getInstance().player
						.sendMessage(new TextComponentString("\u00A77\u00A7o[ERROR]\u00A7c\u00A7o " + msg));
			} catch (Exception e) {
			}
		}
	}

	public static void Fatal(String msg) {

		Log.fatal(msg);
		if (DRPC.logtochat) {
			try {
				Minecraft.getInstance().player
						.sendMessage(new TextComponentString("\u00A77\u00A7o[FATAL]\u00A74\u00A7o " + msg));
			} catch (Exception e) {
			}
		}
	}
}
