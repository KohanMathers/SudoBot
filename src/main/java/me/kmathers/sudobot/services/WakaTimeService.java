package me.kmathers.sudobot.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kmathers.sudobot.config.Config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class WakaTimeService {
    
    private static final HttpClient client = HttpClient.newHttpClient();
    
    public static Map<String, String> getCurrentStatus() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String url = Config.WAKATIME_API_URL + "/users/current/heartbeats?date=" + 
                        today + "&api_key=" + Config.WAKATIME_API_KEY;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 401) {
                System.err.println("Invalid WakaTime API key");
                return null;
            }
            
            if (response.statusCode() != 200) {
                System.err.println("WakaTime API error: " + response.statusCode());
                return null;
            }
            
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray heartbeats = json.getAsJsonArray("data");
            
            if (heartbeats == null || heartbeats.size() == 0) {
                System.out.println("No heartbeats found for today");
                return null;
            }
            
            JsonObject latestHeartbeat = heartbeats.get(heartbeats.size() - 1).getAsJsonObject();
            
            String createdAt = latestHeartbeat.get("created_at").getAsString();
            ZonedDateTime heartbeatTime = ZonedDateTime.parse(createdAt);
            ZonedDateTime now = ZonedDateTime.now();
            
            long minutesAgo = Duration.between(heartbeatTime, now).toMinutes();
            
            String entity = latestHeartbeat.has("entity") ? 
                    latestHeartbeat.get("entity").getAsString() : "";
            String filename = entity.isEmpty() ? "Unknown file" : 
                    entity.substring(entity.lastIndexOf('/') + 1)
                          .substring(entity.lastIndexOf('\\') + 1);
            
            String timeAgoText = formatTimeAgo(minutesAgo);
            
            Map<String, String> result = new HashMap<>();
            result.put("time_ago", timeAgoText);
            result.put("file", filename);
            result.put("project", latestHeartbeat.has("project") ? 
                    latestHeartbeat.get("project").getAsString() : "Unknown project");
            result.put("language", latestHeartbeat.has("language") ? 
                    latestHeartbeat.get("language").getAsString() : "Unknown language");
            result.put("is_coding", String.valueOf(minutesAgo <= 15));
            result.put("minutes_ago", String.valueOf(minutesAgo));
            
            return result;
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error fetching WakaTime status: " + e.getMessage());
            return null;
        }
    }
    
    private static String formatTimeAgo(long minutes) {
        if (minutes == 0) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else {
                return hours + "h " + remainingMinutes + "m ago";
            }
        }
    }
}