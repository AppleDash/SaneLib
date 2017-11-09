package org.appledash.sanelib;

import org.appledash.sanelib.database.DatabaseDebug;
import org.appledash.sanelib.messages.I18nYamlBacked;
import org.appledash.sanelib.messages.II18n;
import org.appledash.sanelib.messages.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by appledash on 6/20/17.
 * Blackjack is best pony.
 */
public abstract class SanePlugin extends JavaPlugin {
    private II18n i18n;
    private MessageUtils messageUtils;

    @Override
    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }

        File translationsFile = new File(this.getDataFolder(), "messages.yml");
        if (translationsFile.exists()) {
            I18nYamlBacked i18nYamlBacked = new I18nYamlBacked(translationsFile);
            this.i18n = i18nYamlBacked;
            this.getLogger().info("Using YAML-backed I18n from " + translationsFile.getAbsolutePath());
            i18nYamlBacked.loadTranslations();
        } else {
            InputStream resourceInputStream = this.getClass().getClassLoader().getResourceAsStream("messages.yml");
            if (resourceInputStream != null) {
                try {
                    Files.copy(resourceInputStream, translationsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    this.getLogger().severe("Failed to copy default translations file!");
                    e.printStackTrace();
                }
                this.getLogger().info("Saved default translations file to '" + translationsFile.getAbsolutePath() + "'.");
            } else {
                this.getLogger().info("No translations file found at '" + translationsFile.getAbsolutePath() + "' (this is not an error) - using default identity I18n.");
            }

            this.i18n = II18n.IDENTITY;
        }
        this.messageUtils = new MessageUtils(this, this.getConfig().getString("chat.prefix", this.getName()));
        DatabaseDebug.setEnabled(this.getConfig().getBoolean("debug", false));
    }

    public final II18n getI18n() {
        return this.i18n;
    }

    public MessageUtils getMessenger() {
        return this.messageUtils;
    }
}
