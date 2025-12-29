package cr.chromapie.bpopener.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import cr.chromapie.bpopener.config.BPOConfig;

public class BPOCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "bpopener";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bpopener <reload|debug>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                BPOConfig.reload();
                sender.addChatMessage(new ChatComponentText("§a[BPOpener] Config reloaded"));
                break;

            case "debug":
                BPOConfig.debug = !BPOConfig.debug;
                sender.addChatMessage(
                    new ChatComponentText("§e[BPOpener] Debug: " + (BPOConfig.debug ? "§aON" : "§cOFF")));
                break;

            default:
                sender.addChatMessage(new ChatComponentText("§cUnknown subcommand: " + subCommand));
                sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsFromIterableMatchingLastWord(args, Arrays.asList("reload", "debug"));
        }
        return null;
    }
}
