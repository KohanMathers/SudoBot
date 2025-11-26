package me.kmathers.sudobot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.Color;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuillDocsCommand {
    
    private static final String BASE_URL = "https://quill.kmathers.co.uk/docs/";
    private static final int MAX_EMBED_LENGTH = 4096;
    private static final int MAX_FIELD_VALUE = 1024;
    
    private static final Map<String, String> DOC_SHORTCUTS = new HashMap<>();
    
    static {
        DOC_SHORTCUTS.put("index", "index.html");
        DOC_SHORTCUTS.put("home", "index.html");
        DOC_SHORTCUTS.put("getting-started", "getting-started.html");
        DOC_SHORTCUTS.put("installation", "installation.html");
        DOC_SHORTCUTS.put("quickstart", "quickstart.html");
        DOC_SHORTCUTS.put("configuration", "configuration.html");
        DOC_SHORTCUTS.put("config", "configuration.html");
        DOC_SHORTCUTS.put("faq", "faq.html");
        
        DOC_SHORTCUTS.put("syntax", "language/syntax.html");
        DOC_SHORTCUTS.put("data-types", "language/data-types.html");
        DOC_SHORTCUTS.put("types", "language/data-types.html");
        DOC_SHORTCUTS.put("functions", "language/functions.html");
        DOC_SHORTCUTS.put("scopes", "language/scopes.html");
        DOC_SHORTCUTS.put("scope", "language/scopes.html");
        DOC_SHORTCUTS.put("events", "language/events.html");
        DOC_SHORTCUTS.put("string-interpolation", "language/string-interpolation.html");
        DOC_SHORTCUTS.put("strings", "language/string-interpolation.html");
        
        DOC_SHORTCUTS.put("player", "api/player.html");
        DOC_SHORTCUTS.put("world", "api/world.html");
        DOC_SHORTCUTS.put("utility", "api/utility.html");
        DOC_SHORTCUTS.put("util", "api/utility.html");
        DOC_SHORTCUTS.put("constructors", "api/constructors.html");
        DOC_SHORTCUTS.put("scope-api", "api/scope.html");
        
        DOC_SHORTCUTS.put("commands", "commands/general.html");
        DOC_SHORTCUTS.put("script-management", "commands/script-management.html");
        DOC_SHORTCUTS.put("scripts", "commands/script-management.html");
        DOC_SHORTCUTS.put("scope-management", "commands/scope-management.html");
        
        DOC_SHORTCUTS.put("player-events", "events/player-events.html");
        DOC_SHORTCUTS.put("block-events", "events/block-events.html");
        DOC_SHORTCUTS.put("entity-events", "events/entity-events.html");
        DOC_SHORTCUTS.put("world-events", "events/world-events.html");
        
        DOC_SHORTCUTS.put("subscopes", "advanced/subscopes.html");
        DOC_SHORTCUTS.put("error-handling", "advanced/error-handling.html");
        DOC_SHORTCUTS.put("errors", "advanced/error-handling.html");
        DOC_SHORTCUTS.put("custom-events", "advanced/custom-events.html");
        DOC_SHORTCUTS.put("persistent-variables", "advanced/persistent-variables.html");
        DOC_SHORTCUTS.put("persistent", "advanced/persistent-variables.html");
        DOC_SHORTCUTS.put("security", "advanced/security.html");
        
        DOC_SHORTCUTS.put("examples", "examples/basic-scripts.html");
        DOC_SHORTCUTS.put("basic-scripts", "examples/basic-scripts.html");
        DOC_SHORTCUTS.put("minigames", "examples/minigames.html");
        
        DOC_SHORTCUTS.put("api-integration", "developer/api-integration.html");
        DOC_SHORTCUTS.put("developer", "developer/api-integration.html");
    }
    
    public void execute(MessageReceivedEvent event, String query) {
        if (query == null || query.trim().isEmpty()) {
            sendUsageEmbed(event);
            return;
        }
        
        String normalizedQuery = query.toLowerCase().trim().replace(" ", "-");
        
        String docPath = findDocPath(normalizedQuery);
        
        if (docPath == null) {
            sendNotFoundEmbed(event, query);
            return;
        }
        
        try {
            String fullUrl = BASE_URL + docPath;
            Document doc = fetchDocument(fullUrl);
            
            if (doc == null) {
                sendErrorEmbed(event, "Failed to fetch documentation", 
                    "Could not retrieve the page at: " + fullUrl);
                return;
            }
            
            sendDocumentationEmbeds(event, doc, fullUrl);
            
        } catch (Exception e) {
            sendErrorEmbed(event, "Error Processing Documentation", 
                "An error occurred: " + e.getMessage());
        }
    }
    
    private String findDocPath(String query) {
        if (DOC_SHORTCUTS.containsKey(query)) {
            return DOC_SHORTCUTS.get(query);
        }
        
        if (DOC_SHORTCUTS.containsKey(query + ".html")) {
            return DOC_SHORTCUTS.get(query + ".html");
        }
        
        for (Map.Entry<String, String> entry : DOC_SHORTCUTS.entrySet()) {
            if (entry.getKey().contains(query) || query.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        if (query.contains("/")) {
            return query.endsWith(".html") ? query : query + ".html";
        }
        
        return null;
    }
    
    private Document fetchDocument(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            
            if (responseCode != 200) {
                return null;
            }
            
            return Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent("QuillBot/1.0")
                    .get();
                    
        } catch (IOException e) {
            return null;
        }
    }
    
    private void sendDocumentationEmbeds(MessageReceivedEvent event, Document doc, String url) {
        String title = doc.select(".page-title").text();
        if (title.isEmpty()) {
            title = doc.select("h1").first() != null ? doc.select("h1").first().text() : "Quill Documentation";
        }
        
        Element mainContent = doc.select(".doc-content").first();
        if (mainContent == null) {
            sendErrorEmbed(event, "Parse Error", "Could not find documentation content");
            return;
        }
        
        List<String> sections = parseContent(mainContent);
        
        List<EmbedBuilder> embeds = new ArrayList<>();
        
        EmbedBuilder mainEmbed = new EmbedBuilder()
                .setTitle("📜 " + title)
                .setColor(new Color(0, 161, 155))
                .setUrl(url);
        
        if (!sections.isEmpty()) {
            String firstSection = sections.get(0);
            if (firstSection.length() > MAX_EMBED_LENGTH) {
                firstSection = firstSection.substring(0, MAX_EMBED_LENGTH - 100) + "...\n\n[Read more](" + url + ")";
            }
            mainEmbed.setDescription(firstSection);
        }
        
        mainEmbed.setFooter("Quill Documentation • Use &&quilldocs <topic> to search");
        embeds.add(mainEmbed);
        
        for (int i = 1; i < Math.min(sections.size(), 5); i++) {
            String section = sections.get(i);
            if (section.length() > MAX_FIELD_VALUE) {
                section = section.substring(0, MAX_FIELD_VALUE - 50) + "...";
            }
            
            if (mainEmbed.getFields().size() < 5) {
                mainEmbed.addField("", section, false);
            }
        }
        
        for (EmbedBuilder embed : embeds) {
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
    
    private List<String> parseContent(Element content) {
        List<String> sections = new ArrayList<>();
        StringBuilder currentSection = new StringBuilder();
        
        Elements children = content.children();
        
        for (Element element : children) {
            String tagName = element.tagName();
            
            if (element.hasClass("toc") || element.hasClass("breadcrumbs") || 
                element.hasClass("page-meta")) {
                continue;
            }
            
            switch (tagName) {
                case "h1":
                case "h2":
                    if (currentSection.length() > 0) {
                        sections.add(currentSection.toString().trim());
                        currentSection = new StringBuilder();
                    }
                    currentSection.append("**").append(element.text()).append("**\n\n");
                    break;
                    
                case "h3":
                    currentSection.append("**").append(element.text()).append("**\n");
                    break;
                    
                case "h4":
                    currentSection.append("*").append(element.text()).append("*\n");
                    break;
                    
                case "p":
                    String text = element.text();
                    if (!text.isEmpty()) {
                        currentSection.append(text).append("\n\n");
                    }
                    break;
                    
                case "ul":
                case "ol":
                    Elements items = element.select("li");
                    for (Element item : items) {
                        currentSection.append("• ").append(item.text()).append("\n");
                    }
                    currentSection.append("\n");
                    break;
                    
                case "pre":
                    String code = element.text();
                    if (!code.isEmpty()) {
                        if (code.length() > 500) {
                            code = code.substring(0, 500) + "\n... (truncated)";
                        }
                        currentSection.append("```\n").append(code).append("\n```\n\n");
                    }
                    break;
                    
                case "blockquote":
                    currentSection.append("> ").append(element.text()).append("\n\n");
                    break;
                    
                case "hr":
                    currentSection.append("─────────────────────\n\n");
                    break;
            }
        }
        
        if (currentSection.length() > 0) {
            sections.add(currentSection.toString().trim());
        }
        
        return sections;
    }
    
    private void sendUsageEmbed(MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📚 Quill Documentation Search")
                .setColor(new Color(0, 161, 155))
                .setDescription("Search the Quill documentation for specific topics.")
                .addField("Usage", "```\n&&quilldocs <topic>\n```", false)
                .addField("Examples", 
                    "```\n" +
                    "&&quilldocs syntax\n" +
                    "&&quilldocs player events\n" +
                    "&&quilldocs subscopes\n" +
                    "&&quilldocs data types\n" +
                    "```", false)
                .addField("Available Topics", 
                    "• **Language**: syntax, data-types, functions, scopes, events\n" +
                    "• **API**: player, world, utility, constructors\n" +
                    "• **Commands**: scripts, scope-management\n" +
                    "• **Events**: player-events, block-events, entity-events\n" +
                    "• **Advanced**: subscopes, error-handling, security\n" +
                    "• **Examples**: basic-scripts, minigames", 
                    false)
                .setFooter("Full documentation: https://quill.kmathers.co.uk/docs/");
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    private void sendNotFoundEmbed(MessageReceivedEvent event, String query) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("❌ Documentation Not Found")
                .setColor(new Color(237, 66, 69))
                .setDescription("Could not find documentation for: **" + query + "**")
                .addField("Suggestions", 
                    "• Try using a more specific term\n" +
                    "• Use `&&quilldocs` without arguments to see available topics\n" +
                    "• Visit the full docs: https://quill.kmathers.co.uk/docs/", 
                    false);
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
    
    private void sendErrorEmbed(MessageReceivedEvent event, String title, String description) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("❌ " + title)
                .setColor(new Color(237, 66, 69))
                .setDescription(description);
        
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}