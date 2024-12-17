package com.github.wohaopa.tc4helper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import cn.elytra.mod.tc4h.TCResearchHelperManager;

public class Command extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "tc4helper";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tc4helper <enable|disable|stop>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayer) {
            if (args.length == 1) {
                switch (args[0]) {
                    case "enable" -> {
                        TC4Helper.enabled = true;
                        sender.addChatMessage(new ChatComponentTranslation("tc4helper.command.message.enable"));
                    }
                    case "disable" -> {
                        TC4Helper.enabled = false;
                        sender.addChatMessage(new ChatComponentTranslation("tc4helper.command.message.disable"));
                    }
                    case "stop" -> {
                        TCResearchHelperManager.forceStop();
                        sender.addChatMessage(new ChatComponentTranslation("tc4helper.command.message.stop"));
                        TC4Helper.LOG.info("Forcibly stopping existing Research task");
                    }
                    default -> sendHelpMessage(sender);
                }
                return;
            }

            sendHelpMessage(sender);
        }

    }

    private static void sendHelpMessage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentTranslation("tc4helper.command.help.0"));
        sender.addChatMessage(new ChatComponentTranslation("tc4helper.command.help.1"));
        sender.addChatMessage(new ChatComponentTranslation("tc4helper.command.help.2"));
        sender.addChatMessage(new ChatComponentTranslation("tc4helper.command.help.3"));
        sender.addChatMessage(new ChatComponentTranslation("tc4helper.command.help.4"));
    }
}
