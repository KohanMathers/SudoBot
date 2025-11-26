package me.kmathers.sudobot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class ConfigCommand {
    
    public void execute(MessageReceivedEvent event, String pluginName) {
        if (pluginName.isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setColor(new Color(237, 66, 69))
                    .setDescription("Please provide a plugin name!\n\n**Usage:** `&&config PluginName`")
                    .setFooter("Example: &&config Essentials");
            
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
            return;
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Config Instructions - " + pluginName)
                .setColor(new Color(0, 161, 155))
                .setDescription(String.format(
                        "Please send the `config.yml` file from the **%s** plugin folder.\n\n" +
                        "You can find it here:",
                        pluginName
                ))
                .addField(
                        "File Location:",
                        String.format("```\nserver/plugins/%s/config.yml\n```", pluginName),
                        false
                )
                .addField(
                        "How to send:",
                        String.format(
                                "1. Locate the file in your server files\n" +
                                "2. Upload it directly to <#%s>\n" +
                                "3. Or paste the contents in a code block:\n" +
                                "\\`\\`\\`yaml\n(paste config here)\n\\`\\`\\`",
                                event.getChannel().getId()
                        ),
                        false
                )
                .setFooter("Make sure to send the entire file!");
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}