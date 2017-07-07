package org.appledash.sanelib;

import org.appledash.sanelib.messages.I18nYamlBacked;
import org.appledash.sanelib.messages.II18n;
import org.appledash.sanelib.messages.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Created by appledash on 6/20/17.
 * Blackjack is best pony.
 */
public abstract class SanePlugin extends JavaPlugin {
    private II18n i18n;
    private MessageUtils messageUtils;

    @Override
    public void onEnable() {
        File translationsFile = new File(this.getDataFolder(), "messages.yml");
        if (translationsFile.exists()) {
            I18nYamlBacked i18nYamlBacked = new I18nYamlBacked(translationsFile);
            this.i18n = i18nYamlBacked;
            this.getLogger().info("Using YAML-backed I18n from " + translationsFile.getAbsolutePath());
            i18nYamlBacked.loadTranslations();
        } else {
            this.i18n = II18n.IDENTITY;
            this.getLogger().info("No translations file found at '" + translationsFile.getAbsolutePath() + "' (this is not an error) - using default identity I18n.");
        }
        this.messageUtils = new MessageUtils(this, this.getConfig().getString("chat.prefix", this.getName()));
    }

    public final II18n getI18n() {
        return this.i18n;
    }

    public MessageUtils getMessenger() {
        return this.messageUtils;
    }
}
