package me.kmathers.sudobot.parsers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionParser {
    
    public static void checkVersion(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        
        if (!content.toLowerCase().contains("this server is running")) {
            return;
        }
        
        String[] lines = content.split("\n");
        String serverType = "";
        String version = "";
        String apiVersion = "";
        String buildInfo = "";
        boolean isLatest = false;
        
        Pattern runningPattern = Pattern.compile("running (\\w+) version ([\\d\\.\\-\\w@]+)");
        Pattern apiPattern = Pattern.compile("Implementing API version ([\\d\\.\\-\\w]+)");
        Pattern buildPattern = Pattern.compile("\\(([\\d\\-T:Z]+)\\)");
        
        for (String line : lines) {
            if (line.contains("This server is running")) {
                Matcher runningMatcher = runningPattern.matcher(line);
                if (runningMatcher.find()) {
                    serverType = runningMatcher.group(1);
                    version = runningMatcher.group(2);
                }
                
                Matcher apiMatcher = apiPattern.matcher(line);
                if (apiMatcher.find()) {
                    apiVersion = apiMatcher.group(1);
                }
                
                Matcher buildMatcher = buildPattern.matcher(line);
                if (buildMatcher.find()) {
                    buildInfo = buildMatcher.group(1);
                }
            }
            
            if (line.contains("You are running the latest version")) {
                isLatest = true;
            }
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Server Version Information")
                .setColor(isLatest ? new Color(87, 242, 135) : new Color(254, 231, 92));
        
        if (!serverType.isEmpty()) {
            embed.addField("Server Type", "```\n" + serverType + "\n```", true);
        }
        
        if (!version.isEmpty()) {
            embed.addField("Version", "```\n" + version + "\n```", true);
        }
        
        embed.addField("Status", isLatest ? "Latest Version" : "Update Available", true);
        
        if (!apiVersion.isEmpty()) {
            embed.addField("API Version", "```\n" + apiVersion + "\n```", false);
        }
        
        if (!buildInfo.isEmpty()) {
            embed.addField("Build Date", "```\n" + buildInfo + "\n```", false);
        }
        
        embed.addField("Raw Output", "||```\n" + content + "\n```||", false);
        embed.setFooter("Parsed from server console output");
        
        event.getChannel()
                .sendMessageEmbeds(embed.build())
                .setMessageReference(event.getMessage())
                .queue();
    }
}