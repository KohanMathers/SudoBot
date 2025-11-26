package me.kmathers.sudobot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class LinksCommand {
    
    public void execute(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Download Links")
                .setColor(new Color(0, 161, 155))
                .addField("Quill", "[GitHub](https://github.com/kohanmathers/quill)", false)
                .addField("InvView", "[Spigot](https://www.spigotmc.org/resources/invview.129244/)", false)
                .addField("MemberMessenger", "[Invite](https://discord.com/oauth2/authorize?client_id=1377995351931486288)", false)
                .addField("PlaceholderNametags", "[Spigot](https://www.spigotmc.org/resources/placeholdernametags.125083/)", false)
                .addField("Pronouns", "[Spigot](https://www.spigotmc.org/resources/pronouns.123867/)", false)
                .addField("StrikeSystem", "[Invite](https://discord.com/oauth2/authorize?client_id=1409667296582045859)", false)
                .addField("TwitchAnnouncer", "[Invite](https://discord.com/oauth2/authorize?client_id=1307780537972166747)", false)
                .setFooter("Links updated 06/11/2025");
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}