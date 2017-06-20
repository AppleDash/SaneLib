package org.appledash.sanelib.messages;

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by AppleDash on 8/5/2016.
 * Blackjack is still best pony.
 */
public class I18n {
    private final Map<String, String> translations = new HashMap<>();
    private final File translationsFile;

    public I18n(File translationsFile) {
        this.translationsFile = translationsFile;
    }

    public void loadTranslations() {
        File configFile = this.translationsFile;
        YamlConfiguration configJar = YamlConfiguration.loadConfiguration(new InputStreamReader(this.getClass().getResourceAsStream("/messages.yml")));

        if (configFile.exists()) { // Attempt to merge any new keys from the JAR's messages.yml into the copy in the plugin's data folder
            YamlConfiguration configDisk = YamlConfiguration.loadConfiguration(configFile);

            List<Map<?, ?>> finalKeys = configDisk.getMapList("messages");

            for (Map jarObject : configJar.getMapList("messages")) { // For every translation in the template config in the JAR
                String jarMessage = String.valueOf(jarObject.get("message")); // Key for this translation
                Map equivalentOnDisk = null; // Equivalent of this translation in the config file on disk

                for (Map diskMap : configDisk.getMapList("messages")) { // For every translation in the config on disk
                    if (String.valueOf(diskMap.get("message")).equals(jarMessage)) { // If the translation key on this object on disk is the same as the current one in the JAR
                        equivalentOnDisk = diskMap;
                        break;
                    }
                }

                if (equivalentOnDisk == null) { // This one isn't on disk yet - add it.
                    finalKeys.add(jarObject);
                } else {
                    String currentKey = String.valueOf(equivalentOnDisk.get("message"));
                    String currentTranslation = String.valueOf(equivalentOnDisk.get("translation"));
                    convertKey(finalKeys, currentKey, currentTranslation);
                }

            }

            for (Map diskObject : configDisk.getMapList("messages")) {
                convertKey(finalKeys, String.valueOf(diskObject.get("message")), String.valueOf(diskObject.get("translation")));
            }

            configDisk.set("messages", finalKeys);

            try {
                configDisk.save(configFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save translations file.", e);
            }
        } else {
            try {
                configJar.save(configFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save initial translations file.", e);
            }
        }

        YamlConfiguration configFileYaml = YamlConfiguration.loadConfiguration(configFile);
        configFileYaml.getMapList("messages").stream().filter(map -> map.containsKey("translation")).forEach(map -> {
            translations.put(map.get("message").toString(), map.get("translation").toString());
        });
    }

    private void convertKey(List<Map<?, ?>> finalKeys, String currentKey, String currentTranslation) {
        String convertedKey = convertOldTranslations(currentKey);

        if (!currentKey.equals(convertedKey)) { // Key needs conversion
            String convertedValue = convertOldTranslations(String.valueOf(currentTranslation));

            // Remove current key from map of things to go to the disk
            finalKeys.removeIf(map -> String.valueOf(map.get("message")).equals(currentKey));

            // Add the converted one.
            if (convertedValue.equals("null")) {
                finalKeys.add(ImmutableMap.of("message", convertedKey));
            } else {
                finalKeys.add(ImmutableMap.of("message", convertedKey, "translation", convertedValue));
            }
        }
    }

    private String convertOldTranslations(String input) {
        Matcher m = Pattern.compile("(%s)").matcher(input);
        StringBuffer converted = new StringBuffer();
        int index = 1;

        while (m.find()) {
            m.appendReplacement(converted, String.format("{%d}", index));
            index++;
        }

        m.appendTail(converted);

        return converted.toString();
    }

    public String translate(String input) {
        return translations.containsKey(input) ? ChatColor.translateAlternateColorCodes('&', translations.get(input)) : input;
    }
}
