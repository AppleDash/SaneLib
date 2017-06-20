package org.appledash.sanelib;

import org.appledash.sanelib.messages.I18n;
import org.appledash.sanelib.messages.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Created by appledash on 6/20/17.
 * Blackjack is best pony.
 */
public abstract class SanePlugin extends JavaPlugin {
    private I18n i18n;
    private MessageUtils messageUtils;

    @Override
    public void onEnable() {
        this.i18n = new I18n(new File(this.getDataFolder(), "messages.yml"));
        this.messageUtils = new MessageUtils(this, this.getConfig().getString("chat.prefix", this.getName()));
    }

    public final I18n getI18n() {
        return this.i18n;
    }

    public MessageUtils getMessenger() {
        return this.messageUtils;
    }
}
