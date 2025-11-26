package me.kmathers.sudobot.config;

public class Config {
    public static final String DISCORD_TOKEN = ConfigManager.getDiscordToken();
    public static final String GITHUB_TOKEN = ConfigManager.getGithubToken();
    public static final String WAKATIME_API_KEY = ConfigManager.getWakatimeToken();
    public static final String REPO_NAME = "kohanmathers/kohanmathers";
    public static final long TARGET_USER_ID = 520872721060462592L;
    
    public static final String GITHUB_API_URL = "https://api.github.com";
    
    public static final String WAKATIME_API_URL = "https://wakatime.com/api/v1";
}

