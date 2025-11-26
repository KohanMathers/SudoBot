package me.kmathers.sudobot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class DefaultCommand {
    
    public void execute(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Unknown command")
                .setColor(new Color(237, 66, 69))
                .setDescription("Please provide a valid command. Alternatively, if you think I should understand this command, open a suggestion in <#1373883749900746795> with the `Discord` tag.");
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}