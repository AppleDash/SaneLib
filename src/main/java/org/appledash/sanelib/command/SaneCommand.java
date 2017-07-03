package org.appledash.sanelib.command;

import org.appledash.sanelib.SanePlugin;
import org.appledash.sanelib.command.exception.CommandException;
import org.appledash.sanelib.command.exception.type.NoPermissionException;
import org.appledash.sanelib.command.exception.type.usage.UsageException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public abstract class SaneCommand implements CommandExecutor {
    protected final SanePlugin plugin;

    public SaneCommand(SanePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                if (!sender.hasPermission(getPermission())) {
                    throw new NoPermissionException();
                }


                onCommand(sender, args);
            } catch (UsageException e) {
                /* Invalid usage in some way, print out exactly what went wrong along with the proper usage. */
                this.plugin.getMessenger().sendMessage(sender, e.getMessage());


                for (String s : getUsage()) {
                    this.plugin.getMessenger().sendMessage(sender, "Usage: {1}", this.plugin.getI18n().translate(s.replace("<command>", label)));
                }
            } catch (CommandException e) {
                this.plugin.getMessenger().sendMessage(sender, e.getMessage());
            }
        });
        return true;
    }

    /**
     * Get the permission node required to use the command.
     * @return Permission node.
     */
    public abstract String getPermission();

    /**
     * Get the command's usage.
     * When this is printed, '<command>' will be replaced with the command name.
     * @return Command usage examples
     */
    public abstract String[] getUsage();

    protected abstract void onCommand(CommandSender sender, String[] args) throws CommandException;
}
