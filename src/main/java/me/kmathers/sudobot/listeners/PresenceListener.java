package me.kmathers.sudobot.listeners;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import me.kmathers.sudobot.config.Config;
import me.kmathers.sudobot.services.GitHubService;
import me.kmathers.sudobot.services.WakaTimeService;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PresenceListener extends ListenerAdapter {
    
    private static String lastTrack = null;
    private static String lastArtist = null;
    private static final Map<String, String> lastWakaTimeData = new HashMap<>();
    
    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent event) {
        Member member = event.getMember();
        
        if (member == null || member.getIdLong() != Config.TARGET_USER_ID) {
            return;
        }
        
        Activity musicActivity = null;
        for (Activity activity : event.getNewValue()) {
            if (activity.getType() == Activity.ActivityType.LISTENING && 
                "Apple Music".equals(activity.getName())) {
                musicActivity = activity;
                break;
            }
        }
        
        String currentTrack;
        String currentArtist;
        
        if (musicActivity != null) {
            RichPresence rp = musicActivity.asRichPresence();

            if (rp != null) {
                currentTrack = rp.getDetails() != null ? rp.getDetails() : "Unknown track";
                currentArtist = rp.getState() != null ? rp.getState() : "Unknown artist";
            } else {
                currentTrack = "Not listening to music";
                currentArtist = "Check again later";
            }
        } else {
            currentTrack = "Not listening to music";
            currentArtist = "Check again later";
        }

        if (!currentTrack.equals(lastTrack) || !currentArtist.equals(lastArtist)) {
            lastTrack = currentTrack;
            lastArtist = currentArtist;
            
            System.out.println("Music changed: " + currentTrack + " by " + currentArtist);
            
            updateReadme(currentTrack, currentArtist);
        }
    }
    
    private void updateReadme(String track, String artist) {
        try {
            Map<String, String> wakaTimeData = WakaTimeService.getCurrentStatus();
            String content = generateStatusMarkdown(track, artist, wakaTimeData);
            GitHubService.updateReadme(content);
            
            lastWakaTimeData.clear();
            if (wakaTimeData != null) {
                lastWakaTimeData.putAll(wakaTimeData);
            }
        } catch (Exception e) {
            System.err.println("Error updating README: " + e.getMessage());
            e.printStackTrace();
        }
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
          .append(java.time.LocalDateTime.now()
                  .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
          .append("*");
        
        return sb.toString();
    }
    
    public static String getLastTrack() {
        return lastTrack;
    }
    
    public static String getLastArtist() {
        return lastArtist;
    }
    
    public static Map<String, String> getLastWakaTimeData() {
        return new HashMap<>(lastWakaTimeData);
    }
}