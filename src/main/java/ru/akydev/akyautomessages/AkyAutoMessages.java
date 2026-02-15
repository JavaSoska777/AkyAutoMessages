package ru.akydev.akyautomessages;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.akydev.akyautomessages.config.ConfigManager;
import ru.akydev.akyautomessages.utils.ColorUtils;

import java.util.List;
import java.util.Random;

public class AkyAutoMessages extends JavaPlugin {
    
    private ConfigManager configManager;
    private int taskId = -1;
    private final Random random = new Random();
    private int messageIndex = 0;
    
    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        startMessageTask();
        getLogger().info("AkyAutoMessages врублён");
    }
    
    @Override
    public void onDisable() {
        stopMessageTask();
        getLogger().info("AkyAutoMessages вырублен");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("akyautomessages")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("akyautomessages.admin")) {
                    sender.sendMessage(ColorUtils.colorize("&cТвар ты дражащая, нету у тя прав"));
                    return true;
                }
                
                configManager.loadConfigs();
                stopMessageTask();
                if (configManager.isEnabled()) {
                    startMessageTask();
                }
                sender.sendMessage(ColorUtils.colorize("&aAkyAutoMessages релоадед "));
                return true;
            }
        }
        return false;
    }
    
    private void startMessageTask() {
        if (!configManager.isEnabled()) {
            return;
        }
        
        int firstDelay = configManager.getFirstDelay() * 20;
        int interval = configManager.getInterval() * 20;
        
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (Bukkit.getOnlinePlayers().size() < configManager.getMinPlayers()) {
                return;
            }
            
            List<ConfigManager.AutoMessage> messages = configManager.getMessages();
            if (messages.isEmpty()) {
                return;
            }
            
            ConfigManager.AutoMessage message;
            if (configManager.isRandom()) {
                message = getWeightedRandomMessage(messages);
            } else {
                message = messages.get(messageIndex % messages.size());
                messageIndex++;
            }
            
            String prefix = configManager.getPrefix();
            String formattedMessage = message.formatMessage(prefix);
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("akyautomessages.receive")) {
                    player.sendMessage(ColorUtils.colorize(formattedMessage));
                }
            }
            
            if (configManager.sendToConsole()) {
                Bukkit.getConsoleSender().sendMessage(ColorUtils.colorize(formattedMessage));
            }
            
        }, firstDelay, interval).getTaskId();
    }
    
    private ConfigManager.AutoMessage getWeightedRandomMessage(List<ConfigManager.AutoMessage> messages) {
        int totalWeight = messages.stream().mapToInt(ConfigManager.AutoMessage::getWeight).sum();
        int randomWeight = random.nextInt(totalWeight);
        
        int currentWeight = 0;
        for (ConfigManager.AutoMessage message : messages) {
            currentWeight += message.getWeight();
            if (randomWeight < currentWeight) {
                return message;
            }
        }
        
        return messages.get(messages.size() - 1);
    }
    
    private void stopMessageTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        messageIndex = 0;
    }
}
