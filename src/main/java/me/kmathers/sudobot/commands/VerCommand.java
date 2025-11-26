package me.kmathers.sudobot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class VerCommand {
    
    public void execute(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Version Check Instructions")
                .setColor(new Color(0, 161, 155))
                .setDescription(String.format(
                        "Please run the following command in your server console or in-game:\n\n" +
                        "```\n/ver\n```\n\n" +
                        "Then paste the **entire output** in <#%s>",
                        event.getChannel().getId()
                ))
                .addField(
                        "What to include:",
                        "• Server version information\n" +
                        "• Any version warnings or notices",
                        false
                )
                .setFooter("This helps us provide better support!");
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}