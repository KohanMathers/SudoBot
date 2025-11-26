package me.kmathers.sudobot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class QuillHelpCommand {
    
    public void execute(MessageReceivedEvent event) {
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Quill Simulator Help")
                .setColor(new Color(0, 161, 155))
                .setDescription("Here is all functions for the Quill Simulator")
                .addField(
                        "Player Instruction",
                        "```\nPLAYER <name> AT <x> <y> <z>\n```\n- Case insensitive (PLAYER, player, Player all work)\n- Player name must be word characters (letters, numbers, underscore)\n- Coordinates can be integers or decimals (e.g., `100` or `100.5`)\n- Whitespace is flexible between tokens",
                        false
                )
                .addField(
                        "Multiple Players",
                        "You can spawn multiple players by using multiple PLAYER instructions:\n```\nPLAYER IEatSystemFiles AT 0 64 0\nPLAYER HannahFaun AT 10 64 10\nPLAYER CurlyNat14 AT 20 64 20\n```",
                        false
                )
                .addField(
                        "Built-in Utility Functions",
                        "```\nlog\nlen\nappend\nremove\ncontains\nsplit\njoin\nto_string\nto_number\nto_boolean\ntype_of\nrange\nrandom\nround\nfloor\nceil\nabs\nsqrt\npow\nrandom_choice\nmin\nmax\nsum\navg\nget_player\nget_online_players\n```",
                        false
                )
                .addField(
                        "Built-in Player Functions",
                        "```\nteleport\ngive\nremove_item\nset_health\nset_hunger\nset_gamemode\nheal\nkill\nsendmessage\nget_health\nget_hunger\nget_name\nget_location\nget_gamemode\nset_flying\ndamage\n```",
                        false
                )
                .addField(
                        "Built-in World Functions",
                        "```\nset_block\nget_block\nbreak_block\nset_time\nget_time\nset_weather\nget_weather\nbroadcast\ncreate_world\nget_world\nspawn_entity\ndistance\n```",
                        false
                )
                .setFooter("All functions use the same syntax as Quill");
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}