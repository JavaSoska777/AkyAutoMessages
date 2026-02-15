package ru.akydev.akyautomessages.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#([A-Fa-f0-9]{6})-#([A-Fa-f0-9]{6})>(.*?)</gradient>");
    private static final boolean HAS_HEX_SUPPORT;
    
    static {
        boolean hasHex = false;
        try {
            Class<?> chatColorClass = ChatColor.class;
            Method ofMethod = chatColorClass.getMethod("of", String.class);
            hasHex = true;
        } catch (NoSuchMethodException e) {
            hasHex = false;
        }
        HAS_HEX_SUPPORT = hasHex;
    }
    
    public static String colorize(String message) {
        if (message == null) return "";
        
        if (HAS_HEX_SUPPORT) {
            message = translateHexColors(message);
            message = translateGradients(message);
        }
        
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }
    
    private static String translateHexColors(String message) {
        if (!HAS_HEX_SUPPORT) return message;
        
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        try {
            Method ofMethod = ChatColor.class.getMethod("of", String.class);
            
            while (matcher.find()) {
                String hex = matcher.group(1);
                ChatColor color = (ChatColor) ofMethod.invoke(null, "#" + hex);
                matcher.appendReplacement(buffer, color.toString());
            }
        } catch (Exception e) {
            return message;
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    private static String translateGradients(String message) {
        if (!HAS_HEX_SUPPORT) return message;
        
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String text = matcher.group(3);
            
            String gradient = createGradient(text, startHex, endHex);
            matcher.appendReplacement(buffer, gradient);
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    
    private static String createGradient(String text, String startHex, String endHex) {
        if (text.isEmpty() || !HAS_HEX_SUPPORT) return "";
        
        StringBuilder result = new StringBuilder();
        int length = text.length();
        
        int startR = Integer.parseInt(startHex.substring(0, 2), 16);
        int startG = Integer.parseInt(startHex.substring(2, 4), 16);
        int startB = Integer.parseInt(startHex.substring(4, 6), 16);
        
        int endR = Integer.parseInt(endHex.substring(0, 2), 16);
        int endG = Integer.parseInt(endHex.substring(2, 4), 16);
        int endB = Integer.parseInt(endHex.substring(4, 6), 16);
        
        try {
            Method ofMethod = ChatColor.class.getMethod("of", String.class);
            
            for (int i = 0; i < length; i++) {
                double ratio = (double) i / (length - 1);
                
                int r = (int) (startR + (endR - startR) * ratio);
                int g = (int) (startG + (endG - startG) * ratio);
                int b = (int) (startB + (endB - startB) * ratio);
                
                String hex = String.format("#%02X%02X%02X", r, g, b);
                ChatColor color = (ChatColor) ofMethod.invoke(null, hex);
                result.append(color).append(text.charAt(i));
            }
        } catch (Exception e) {
            return text;
        }
        
        return result.toString();
    }
}
