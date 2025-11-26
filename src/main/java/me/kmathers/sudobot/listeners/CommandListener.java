package me.kmathers.sudobot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import me.kmathers.sudobot.commands.*;
import me.kmathers.sudobot.parsers.ConfigParser;
import me.kmathers.sudobot.parsers.VersionParser;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        
        String content = event.getMessage().getContentRaw();
        
        if (content.contains("&!")) {
            return;
        }
        
        if (content.startsWith("&&")) {
            handleCommand(event, content);
            return;
        }
        
        VersionParser.checkVersion(event);
        
        ConfigParser.checkConfig(event);
    }
    
    private void handleCommand(MessageReceivedEvent event, String content) {
        String raw = content.substring(2).trim();
        int newline = raw.indexOf('\n');

        String command;
        String args;

        if (newline == -1) {
            String[] split = raw.split(" ", 2);
            command = split[0].toLowerCase();
            args = split.length > 1 ? split[1] : "";
        } else {
            command = raw.substring(0, newline).trim().toLowerCase();
            args = raw.substring(newline + 1);
        }
        
        switch (command) {
            case "links":
                new LinksCommand().execute(event);
                break;
            case "ver":
                new VerCommand().execute(event);
                break;
            case "prndebug":
                new PrnDebugCommand().execute(event);
                break;
            case "config":
                new ConfigCommand().execute(event, args);
                break;
            case "quill":
                new QuillCommand().execute(event, args);
                break;
            case "quillhelp":
                new QuillHelpCommand().execute(event);
                break;
            case "quilldocs":
                new QuillDocsCommand().execute(event, args);
                break;
            default:
                new DefaultCommand().execute(event);
                break;
        }
    }
}