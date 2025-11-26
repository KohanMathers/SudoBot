package me.kmathers.sudobot.tasks;

import me.kmathers.sudobot.listeners.PresenceListener;
import me.kmathers.sudobot.services.GitHubService;
import me.kmathers.sudobot.services.WakaTimeService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class WakaTimeUpdateTask implements Runnable {
    
    private static Map<String, String> lastWakaTimeData = null;
    
    @Override
    public void run() {
        try {
            Map<String, String> currentWakaTimeData = WakaTimeService.getCurrentStatus();
            
            if (hasChanged(currentWakaTimeData, lastWakaTimeData)) {
                System.out.println("WakaTime status changed, updating README...");
                
                String track = PresenceListener.getLastTrack();
                String artist = PresenceListener.getLastArtist();
                
                if (track == null) {
                    track = "Not listening to music";
                    artist = "Check again later";
                }
                
                String content = generateStatusMarkdown(track, artist, currentWakaTimeData);
                GitHubService.updateReadme(content);
                lastWakaTimeData = currentWakaTimeData;
            }
            
        } catch (Exception e) {
            System.err.println("Error in periodic WakaTime update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean hasChanged(Map<String, String> current, Map<String, String> last) {
        if (current == null && last == null) return false;
        if (current == null || last == null) return true;
        
        return !current.equals(last);
    }
    
    private String generateStatusMarkdown(String track, String artist, 
                                         Map<String, String> wakaTimeData) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("- 🎵 **Now Playing:** ").append(track).append(" - ").append(artist).append("\n");
        
        if (wakaTimeData != null && !wakaTimeData.isEmpty()) {
            boolean isCoding = Boolean.parseBoolean(wakaTimeData.get("is_coding"));
            String language = wakaTimeData.get("language");
            String file = wakaTimeData.get("file");
            String project = wakaTimeData.get("project");
            String timeAgo = wakaTimeData.get("time_ago");
            
            if (isCoding) {
                sb.append("- 💻 **Currently coding** in *").append(language)
                  .append("*, editing `").append(file)
                  .append("` (Project: ").append(project)
                  .append(") - ").append(timeAgo).append("\n");
            } else {
                sb.append("- 💻 **Last seen coding** in *").append(language)
                  .append("*, editing `").append(file)
                  .append("` (Project: ").append(project)
                  .append(") - ").append(timeAgo).append("\n");
            }
        } else {
            sb.append("- 💻 **Coding Status:** Not coding\n");
        }
        
        sb.append("\n*Last updated: ")
          .append(LocalDateTime.now()
                  .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
          .append("*");
        
        return sb.toString();
    }
}