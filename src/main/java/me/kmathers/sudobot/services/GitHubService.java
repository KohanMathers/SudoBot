package me.kmathers.sudobot.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kmathers.sudobot.config.Config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubService {
    
    private static final HttpClient client = HttpClient.newHttpClient();
    
    public static void updateReadme(String content) {
        try {
            String url = Config.GITHUB_API_URL + "/repos/" + Config.REPO_NAME + "/contents/README.md";
            
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "token " + Config.GITHUB_TOKEN)
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();
            
            HttpResponse<String> getResponse = client.send(getRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (getResponse.statusCode() != 200) {
                System.err.println("Failed to get README: " + getResponse.statusCode());
                return;
            }
            
            JsonObject readme = JsonParser.parseString(getResponse.body()).getAsJsonObject();
            String sha = readme.get("sha").getAsString();
            String encodedContent = readme.get("content").getAsString();
            
            String readmeContent = new String(
                    Base64.getMimeDecoder().decode(encodedContent), 
                    StandardCharsets.UTF_8
            );
            
            Pattern pattern = Pattern.compile(
                    "<!-- NOW_PLAYING -->.*?<!-- END_NOW_PLAYING -->", 
                    Pattern.DOTALL
            );
            String replacement = "<!-- NOW_PLAYING -->\n" + content + "\n<!-- END_NOW_PLAYING -->";
            Matcher matcher = pattern.matcher(readmeContent);
            String newReadme = matcher.replaceAll(replacement);
            
            String newEncodedContent = Base64.getEncoder()
                    .encodeToString(newReadme.getBytes(StandardCharsets.UTF_8));
            
            JsonObject updateBody = new JsonObject();
            updateBody.addProperty("message", "Update now playing in README");
            updateBody.addProperty("content", newEncodedContent);
            updateBody.addProperty("sha", sha);
            
            HttpRequest putRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "token " + Config.GITHUB_TOKEN)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(updateBody.toString()))
                    .build();
            
            HttpResponse<String> putResponse = client.send(putRequest, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (putResponse.statusCode() == 200) {
                System.out.println("README updated successfully");
            } else {
                System.err.println("Failed to update README: " + putResponse.statusCode());
            }
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error updating README: " + e.getMessage());
        }
    }
}