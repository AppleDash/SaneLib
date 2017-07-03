package org.appledash.sanelib.messages;

import com.google.common.base.Strings;
import org.appledash.sanelib.SanePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class MessageUtils {
    private final SanePlugin plugin;
    private final String chatPrefix;

    public MessageUtils(SanePlugin plugin, String chatPrefix) {
        this.plugin = plugin;
        this.chatPrefix = chatPrefix;
    }

    /**
     * Send a formatted chat message to the given target.
     * This message will have the prefix defined in SaneEconomy's config file.
     * @param target Target CommandSender
     * @param fmt String#format format
     * @param args String#format args
     */
    public void sendMessage(CommandSender target, String fmt, Object... args) {
        fmt = this.plugin.getI18n().translate(fmt);

        String prefix = ChatColor.translateAlternateColorCodes('&', this.chatPrefix);

        String formatted;

        if (fmt.contains("%s")) { // Legacy support.
            formatted = String.format(fmt, (Object[]) args);
        } else {
            formatted = indexedFormat(fmt, (Object[]) args);
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> target.sendMessage(prefix + formatted));
    }

    public void sendMessage(Object target, String fmt, Object... args) {
        if ((target instanceof OfflinePlayer) && ((OfflinePlayer) target).isOnline() && (target instanceof CommandSender)) {
            this.sendMessage(((CommandSender) target), fmt, (Object[])args);
        }
    }

    public static String indexedFormat(String fmt, Object... arguments) {
        Matcher m = Pattern.compile("\\{([0-9]+)(:[^}]+)?\\}").matcher(fmt);
        StringBuffer formatted = new StringBuffer();

        while (m.find()) {
            int index = Integer.valueOf(m.group(1)) - 1;

            if (index > arguments.length - 1 || index < 0) {
                throw new IllegalArgumentException("Index must be within the range of the given arguments.");
            }

            String stringRep;

            if (!Strings.isNullOrEmpty(m.group(2))) {
                stringRep = String.format(String.format("%%%s", m.group(2).substring(1)), arguments[index]);
            } else {
                stringRep = String.valueOf(arguments[index]);
            }

            m.appendReplacement(formatted, stringRep);
        }

        m.appendTail(formatted);

        return formatted.toString();
    }
}
