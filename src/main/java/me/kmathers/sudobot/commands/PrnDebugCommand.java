package me.kmathers.sudobot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class PrnDebugCommand {
    
    public void execute(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Pronouns Debug Instructions")
                .setColor(new Color(0, 161, 155))
                .setDescription("Follow these steps to debug the Pronouns plugin:")
                .addField(
                        "Step 1: Prepare",
                        "Make sure you have **2 people online** with **different pronouns** set",
                        false
                )
                .addField(
                        "Step 2: Test Player 1",
                        "Run the following command (replace `<player>` with the first player's name):\n" +
                        "```\n/papi parse <player> %pronouns%\n```\n" +
                        "**Example:** `/papi parse IEatSystemFiles %pronouns%`",
                        false
                )
                .addField(
                        "Step 3: Test Player 2",
                        "Run the same command for the second player with different pronouns",
                        false
                )
                .addField(
                        "Step 4: Verify",
                        "Check if the outputted pronouns are correct:\n" +
                        "• If each player shows their own pronouns = **Working correctly**\n" +
                        "• If they display the same pronouns = **Plugin bug**\n" +
                        "• If they **only** show the command runner's pronouns = **Plugin bug**",
                        false
                )
                .setFooter("Paste the results here for further assistance");
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}