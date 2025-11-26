package me.kmathers.sudobot.parsers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.yaml.snakeyaml.Yaml;

import java.awt.Color;
import java.util.Map;

public class ConfigParser {
    
    public static void checkConfig(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        
        String yamlContent = extractYamlFromCodeblock(content);
        if (yamlContent == null) {
            return;
        }
        
        try {
            Yaml yaml = new Yaml();
            Object data = yaml.load(yamlContent);
            
            if (!(data instanceof Map)) {
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> configMap = (Map<String, Object>) data;
            
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Configuration Overview")
                    .setColor(new Color(0, 161, 155))
                    .setDescription("Here are the active configuration options:");
            
            int fieldCount = 0;
            for (Map.Entry<String, Object> entry : configMap.entrySet()) {
                if (fieldCount >= 25) {
                    break;
                }
                
                String key = entry.getKey();
                if (key.startsWith("#")) {
                    continue;
                }
                
                String value = formatYamlValue(entry.getValue(), 0);
                
                if (value.length() > 1024) {
                    value = value.substring(0, 1020) + "...";
                }
                
                embed.addField(key, "```yaml\n" + value + "\n```", false);
                fieldCount++;
            }
            
            if (fieldCount == 0) {
                embed.setDescription("No configuration options found or all are commented out.");
            }
            
            embed.setFooter("Parsed from config.yml");
            
            event.getChannel()
                    .sendMessageEmbeds(embed.build())
                    .setMessageReference(event.getMessage())
                    .queue();
                    
        } catch (Exception e) {
            // Not a valid YAML or error parsing
        }
    }
    
    private static String extractYamlFromCodeblock(String content) {
        if (!content.contains("```")) {
            return null;
        }
        
        String[] parts = content.split("```");
        if (parts.length < 2) {
            return null;
        }
        
        String codeContent = parts[1];
        
        if (codeContent.startsWith("yaml\n")) {
            codeContent = codeContent.substring(5);
        } else if (codeContent.startsWith("yml\n")) {
            codeContent = codeContent.substring(4);
        } else {
            int firstNewline = codeContent.indexOf('\n');
            if (firstNewline > 0 && firstNewline < 20) {
                String firstLine = codeContent.substring(0, firstNewline);
                if (!firstLine.contains(":") && !firstLine.startsWith("#")) {
                    codeContent = codeContent.substring(firstNewline + 1);
                }
            }
        }
        
        return codeContent;
    }
    
    private static String formatYamlValue(Object value, int depth) {
        if (value == null) {
            return "null";
        }
        
        String indent = "  ".repeat(depth);
        
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            if (map.isEmpty()) {
                return "{}";
            }
            
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (count++ >= 5) break;
                sb.append(indent).append(entry.getKey()).append(": ")
                  .append(formatYamlValue(entry.getValue(), depth + 1))
                  .append("\n");
            }
            return sb.toString().trim();
            
        } else if (value instanceof Iterable) {
            Iterable<?> list = (Iterable<?>) value;
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Object item : list) {
                if (count++ >= 5) break;
                sb.append("- ").append(formatYamlValue(item, depth + 1)).append("\n");
            }
            return sb.length() > 0 ? sb.toString().trim() : "[]";
            
        } else {
            return value.toString();
        }
    }
}