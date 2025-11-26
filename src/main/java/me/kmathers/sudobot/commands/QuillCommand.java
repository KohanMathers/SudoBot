package me.kmathers.sudobot.commands;

import me.kmathers.sudobot.quill.interpreter.DiscordQuillInterpreter;
import me.kmathers.sudobot.quill.lexer.QuillLexer;
import me.kmathers.sudobot.quill.parser.AST;
import me.kmathers.sudobot.quill.parser.QuillParser;
import me.kmathers.sudobot.quill.simulation.DiscordSimulationContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuillCommand {
    
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:quill)?\\n?(.*?)```", Pattern.DOTALL);
    private static final Pattern PLAYER_INSTRUCTION_PATTERN = Pattern.compile("PLAYER\\s+(\\w+)\\s+AT\\s+([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)", Pattern.CASE_INSENSITIVE);
    
    public void execute(MessageReceivedEvent event, String messageContent) {
        List<String> codeBlocks = extractCodeBlocks(messageContent);
        
        if (codeBlocks.isEmpty()) {
            sendErrorEmbed(event, "No Code Found", 
                "Please provide Quill code in a code block.\n\n" +
                "**Usage:**\n" +
                "```\n" +
                "&&quill\n" +
                "```quill\n" +
                "let x = 5\n" +
                "log(x * 2)\n" +
                "```\n" +
                "```");
            return;
        }
        
        String instructions = messageContent;
        String mainCode = codeBlocks.get(codeBlocks.size() - 1);
        
        DiscordSimulationContext context = new DiscordSimulationContext();
        
        try {
            processPlayerInstructions(instructions, context);
            
            DiscordQuillInterpreter interpreter = new DiscordQuillInterpreter(context);
            
            QuillLexer lexer = new QuillLexer(mainCode);
            List<QuillLexer.Token> tokens = lexer.tokenize();
            
            QuillParser parser = new QuillParser(tokens);
            AST.Program program = parser.parse();
            
            interpreter.execute(program);
            
            processQueuedEvents(interpreter, context);
            
            sendSuccessEmbed(event, context);
            
        } catch (QuillLexer.LexerException e) {
            sendErrorEmbed(event, "Lexer Error", formatLexerError(e));
        } catch (QuillParser.ParseException e) {
            sendErrorEmbed(event, "Parser Error", formatParserError(e));
        } catch (RuntimeException e) {
            sendErrorEmbed(event, "Runtime Error", formatRuntimeError(e));
        } catch (Exception e) {
            sendErrorEmbed(event, "Unknown Error", 
                "An unexpected error occurred:\n```\n" + e.getMessage() + "\n```");
        }
    }
    
    /**
     * Extract all code blocks from the message content
     */
    private List<String> extractCodeBlocks(String content) {
        List<String> blocks = new ArrayList<>();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(content);
        
        while (matcher.find()) {
            blocks.add(matcher.group(1).trim());
        }
        
        return blocks;
    }
    
    /**
     * Process PLAYER instructions to spawn mock players
     */
    private void processPlayerInstructions(String instructions, DiscordSimulationContext context) {
        Matcher matcher = PLAYER_INSTRUCTION_PATTERN.matcher(instructions);
        
        while (matcher.find()) {
            String playerName = matcher.group(1);
            double x = Double.parseDouble(matcher.group(2));
            double y = Double.parseDouble(matcher.group(3));
            double z = Double.parseDouble(matcher.group(4));
            
            context.spawnPlayer(playerName, x, y, z);
        }
    }
    
    /**
     * Process any events that were queued during script execution
     */
    private void processQueuedEvents(DiscordQuillInterpreter interpreter, DiscordSimulationContext context) {
        int maxEventIterations = 100;
        int iterations = 0;
        
        while (context.hasPendingEvents() && iterations < maxEventIterations) {
            DiscordSimulationContext.SimulatedEvent event = context.getNextEvent();
            if (event != null) {
                interpreter.triggerEvent(event.getEventName(), event.getContext());
            }
            iterations++;
        }
        
        if (iterations >= maxEventIterations) {
            context.log("[WARNING] Event processing stopped after " + maxEventIterations + " iterations");
        }
    }
    
    /**
     * Send success embed with execution results
     */
    private void sendSuccessEmbed(MessageReceivedEvent event, DiscordSimulationContext context) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Quill Execution Complete")
                .setColor(new Color(87, 242, 135));
        
        List<String> logs = context.getLogs();
        if (!logs.isEmpty()) {
            StringBuilder logOutput = new StringBuilder();
            int lineCount = 0;
            for (String log : logs) {
                if (lineCount >= 20) {
                    logOutput.append("... (").append(logs.size() - 20).append(" more lines)");
                    break;
                }
                logOutput.append(log).append("\n");
                lineCount++;
            }
            
            embed.addField("Execution Logs", "```\n" + logOutput.toString() + "```", false);
        }
        
        List<String> messages = context.getMessageHistory();
        if (!messages.isEmpty()) {
            StringBuilder msgOutput = new StringBuilder();
            for (String msg : messages) {
                msgOutput.append(msg).append("\n");
            }
            embed.addField("Messages", "```\n" + msgOutput.toString() + "```", false);
        }
        
        if (!context.getAllPlayers().isEmpty()) {
            StringBuilder playerStates = new StringBuilder();
            for (DiscordSimulationContext.MockPlayer player : context.getAllPlayers()) {
                playerStates.append(String.format(
                    "%s | HP: %.1f | Pos: (%.1f, %.1f, %.1f) | Mode: %s\n",
                    player.getName(),
                    player.getHealth(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getGamemode()
                ));
            }
            embed.addField("Player States", "```\n" + playerStates.toString() + "```", false);
        }
        
        if (context.getAllWorlds().size() > 1 || context.getDefaultWorld().getTime() != 0) {
            StringBuilder worldStates = new StringBuilder();
            for (DiscordSimulationContext.MockWorld world : context.getAllWorlds()) {
                worldStates.append(String.format(
                    "%s | Time: %d | Weather: %s\n",
                    world.getName(),
                    world.getTime(),
                    world.getWeather()
                ));
            }
            embed.addField("World States", "```\n" + worldStates.toString() + "```", false);
        }
        
        embed.setFooter("Executed " + logs.size() + " operations");
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    /**
     * Send error embed
     */
    private void sendErrorEmbed(MessageReceivedEvent event, String title, String description) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("❌ " + title)
                .setColor(new Color(237, 66, 69))
                .setDescription(description);
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    /**
     * Format lexer error message
     */
    private String formatLexerError(QuillLexer.LexerException e) {
        return "Failed to tokenize the code:\n```\n" + e.getMessage() + "\n```\n\n" +
               "Check for:\n" +
               "• Unclosed strings\n" +
               "• Invalid characters\n" +
               "• Unterminated comments";
    }
    
    /**
     * Format parser error message
     */
    private String formatParserError(QuillParser.ParseException e) {
        return "Failed to parse the code:\n```\n" + e.getMessage() + "\n```\n\n" +
               "Check for:\n" +
               "• Missing semicolons\n" +
               "• Unmatched brackets/parentheses\n" +
               "• Invalid syntax";
    }
    
    /**
     * Format runtime error message
     */
    private String formatRuntimeError(RuntimeException e) {
        String message = e.getMessage();
        
        if (message == null) {
            message = "Unknown runtime error occurred";
        }
        
        return "Error during script execution:\n```\n" + message + "\n```\n\n" +
               "Common causes:\n" +
               "• Undefined variables\n" +
               "• Type mismatches\n" +
               "• Division by zero\n" +
               "• Invalid function arguments";
    }
}
