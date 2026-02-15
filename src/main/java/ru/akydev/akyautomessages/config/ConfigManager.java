package ru.akydev.akyautomessages.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.akydev.akyautomessages.AkyAutoMessages;

import java.io.File;
import java.util.*;

public class ConfigManager {
    
    private final AkyAutoMessages plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    
    public ConfigManager(AkyAutoMessages plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        createDefaultConfig();
        createDefaultMessages();
        
        config = plugin.getConfig();
        File messagesFile = new File(plugin.getDataFolder(), "automessages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    private void createDefaultConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("enabled")) {
            config.set("enabled", true);
        }
        if (!config.contains("interval")) {
            config.set("interval", 60);
        }
        if (!config.contains("random")) {
            config.set("random", true);
        }
        if (!config.contains("prefix")) {
            config.set("prefix", "&8[&bAky&fAuto&8] &r");
        }
        
        if (!config.contains("display.console")) {
            config.set("display.console", false);
        }
        if (!config.contains("display.min-players")) {
            config.set("display.min-players", 1);
        }
        if (!config.contains("display.first-delay")) {
            config.set("display.first-delay", 30);
        }
        
        if (!config.contains("messages.allow-empty-lines")) {
            config.set("messages.allow-empty-lines", false);
        }
        if (!config.contains("messages.max-lines")) {
            config.set("messages.max-lines", 5);
        }
        
        plugin.saveConfig();
    }
    
    private void createDefaultMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "automessages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("automessages.yml", false);
        }
    }
    
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    public int getInterval() {
        return config.getInt("interval", 60);
    }
    
    public boolean isRandom() {
        return config.getBoolean("random", true);
    }
    
    public String getPrefix() {
        return config.getString("prefix", "&8[&bAky&fAuto&8] &r");
    }
    
    public boolean sendToConsole() {
        return config.getBoolean("display.console", false);
    }
    
    public int getMinPlayers() {
        return config.getInt("display.min-players", 1);
    }
    
    public int getFirstDelay() {
        return config.getInt("display.first-delay", 30);
    }
    
    public boolean allowEmptyLines() {
        return config.getBoolean("messages.allow-empty-lines", false);
    }
    
    public int getMaxLines() {
        return config.getInt("messages.max-lines", 5);
    }
    
    public List<AutoMessage> getMessages() {
        List<AutoMessage> result = new ArrayList<>();
        ConfigurationSection messagesSection = messages.getConfigurationSection("messages");
        
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                ConfigurationSection messageSection = messagesSection.getConfigurationSection(key);
                if (messageSection != null) {
                    boolean enabled = messageSection.getBoolean("enabled", true);
                    if (!enabled) continue;
                    
                    int weight = messageSection.getInt("weight", 1);
                    List<String> lines = messageSection.getStringList("lines");
                    
                    if (!allowEmptyLines()) {
                        lines.removeIf(String::isEmpty);
                    }
                    
                    if (!lines.isEmpty() && lines.size() <= getMaxLines()) {
                        result.add(new AutoMessage(key, lines, weight));
                    }
                }
            }
        }
        
        return result;
    }
    
    public static class AutoMessage {
        private final String id;
        private final List<String> lines;
        private final int weight;
        
        public AutoMessage(String id, List<String> lines, int weight) {
            this.id = id;
            this.lines = new ArrayList<>(lines);
            this.weight = weight;
        }
        
        public String getId() {
            return id;
        }
        
        public List<String> getLines() {
            return new ArrayList<>(lines);
        }
        
        public int getWeight() {
            return weight;
        }
        
        public String formatMessage(String prefix) {
            StringBuilder result = new StringBuilder();
            for (String line : lines) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(prefix).append(line);
            }
            return result.toString();
        }
    }
}
