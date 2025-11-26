package me.kmathers.sudobot.quill.simulation;

import me.kmathers.sudobot.quill.interpreter.QuillValue;

import java.util.*;

/**
 * Manages simulation state for Quill script execution in Discord context.
 * Tracks mock objects, events, and execution output.
 */
public class DiscordSimulationContext {
    private Map<String, MockPlayer> players;
    private Map<String, MockWorld> worlds;
    private List<String> outputLogs;
    private List<String> messageHistory;
    private Queue<SimulatedEvent> eventQueue;
    private Map<String, Object> globalState;
    
    public DiscordSimulationContext() {
        this.players = new HashMap<>();
        this.worlds = new HashMap<>();
        this.outputLogs = new ArrayList<>();
        this.messageHistory = new ArrayList<>();
        this.eventQueue = new LinkedList<>();
        this.globalState = new HashMap<>();
        
        createWorld("world");
    }
    
    // === Logging ===
    
    /**
     * Log a message to the output logs.
     */
    public void log(String message) {
        outputLogs.add(message);
    }
    
    /**
     * Log a formatted message.
     */
    public void logf(String format, Object... args) {
        log(String.format(format, args));
    }
    
    /**
     * Get all output logs.
     */
    public List<String> getLogs() {
        return new ArrayList<>(outputLogs);
    }
    
    /**
     * Get output logs as a single formatted string.
     */
    public String getFormattedOutput() {
        return String.join("\n", outputLogs);
    }
    
    /**
     * Clear all output logs.
     */
    public void clearLogs() {
        outputLogs.clear();
    }
    
    // === Player Management ===
    
    /**
     * Create a new mock player.
     */
    public MockPlayer spawnPlayer(String name, double x, double y, double z) {
        if (players.containsKey(name)) {
            throw new RuntimeException("Player '" + name + "' already exists");
        }
        
        MockPlayer player = new MockPlayer(name, x, y, z);
        players.put(name, player);
        log("[SPAWN] Player '" + name + "' spawned at (" + x + ", " + y + ", " + z + ")");
        return player;
    }
    
    /**
     * Get a player by name.
     */
    public MockPlayer getPlayer(String name) {
        MockPlayer player = players.get(name);
        if (player == null) {
            throw new RuntimeException("Player '" + name + "' not found");
        }
        return player;
    }
    
    /**
     * Check if a player exists.
     */
    public boolean hasPlayer(String name) {
        return players.containsKey(name);
    }
    
    /**
     * Get all players.
     */
    public Collection<MockPlayer> getAllPlayers() {
        return new ArrayList<>(players.values());
    }
    
    /**
     * Remove a player.
     */
    public void removePlayer(String name) {
        MockPlayer player = players.remove(name);
        if (player != null) {
            log("[REMOVE] Player '" + name + "' removed");
        }
    }
    
    // === World Management ===
    
    /**
     * Create a new mock world.
     */
    public MockWorld createWorld(String name) {
        if (worlds.containsKey(name)) {
            throw new RuntimeException("World '" + name + "' already exists");
        }
        
        MockWorld world = new MockWorld(name);
        worlds.put(name, world);
        log("[WORLD] Created world '" + name + "'");
        return world;
    }
    
    /**
     * Get a world by name.
     */
    public MockWorld getWorld(String name) {
        MockWorld world = worlds.get(name);
        if (world == null) {
            throw new RuntimeException("World '" + name + "' not found");
        }
        return world;
    }
    
    /**
     * Check if a world exists.
     */
    public boolean hasWorld(String name) {
        return worlds.containsKey(name);
    }
    
    /**
     * Get the default world.
     */
    public MockWorld getDefaultWorld() {
        return getWorld("world");
    }
    
    /**
     * Get all worlds.
     */
    public Collection<MockWorld> getAllWorlds() {
        return new ArrayList<>(worlds.values());
    }
    
    // === Event Management ===
    
    /**
     * Queue an event to be triggered.
     */
    public void queueEvent(String eventName, Map<String, QuillValue> context) {
        eventQueue.offer(new SimulatedEvent(eventName, context));
        log("[QUEUE] Event queued: " + eventName);
    }
    
    /**
     * Get the next queued event.
     */
    public SimulatedEvent getNextEvent() {
        return eventQueue.poll();
    }
    
    /**
     * Check if there are pending events.
     */
    public boolean hasPendingEvents() {
        return !eventQueue.isEmpty();
    }
    
    /**
     * Get the number of pending events.
     */
    public int getPendingEventCount() {
        return eventQueue.size();
    }
    
    /**
     * Clear all pending events.
     */
    public void clearEventQueue() {
        eventQueue.clear();
    }
    
    // === Message History ===
    
    /**
     * Add a message to history (e.g., from sendmessage()).
     */
    public void addMessage(String playerName, String message) {
        messageHistory.add(playerName + ": " + message);
        log("[MSG] " + playerName + ": " + message);
    }
    
    /**
     * Get all messages.
     */
    public List<String> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
    
    /**
     * Clear message history.
     */
    public void clearMessageHistory() {
        messageHistory.clear();
    }
    
    // === Global State ===
    
    /**
     * Store a global state value.
     */
    public void setState(String key, Object value) {
        globalState.put(key, value);
    }
    
    /**
     * Get a global state value.
     */
    public Object getState(String key) {
        return globalState.get(key);
    }
    
    /**
     * Check if a state key exists.
     */
    public boolean hasState(String key) {
        return globalState.containsKey(key);
    }
    
    /**
     * Get all global state.
     */
    public Map<String, Object> getAllState() {
        return new HashMap<>(globalState);
    }
    
    /**
     * Clear all global state.
     */
    public void clearState() {
        globalState.clear();
    }
    
    // === Summary ===
    
    /**
     * Get a summary of the current simulation state.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== Simulation Summary ===\n");
        sb.append("Players: ").append(players.size()).append("\n");
        for (MockPlayer player : players.values()) {
            sb.append("  - ").append(player.getName())
              .append(" at (").append(String.format("%.1f", player.getX()))
              .append(", ").append(String.format("%.1f", player.getY()))
              .append(", ").append(String.format("%.1f", player.getZ()))
              .append(")\n");
        }
        
        sb.append("Worlds: ").append(worlds.size()).append("\n");
        for (MockWorld world : worlds.values()) {
            sb.append("  - ").append(world.getName()).append("\n");
        }
        
        sb.append("Pending Events: ").append(eventQueue.size()).append("\n");
        sb.append("Messages: ").append(messageHistory.size()).append("\n");
        sb.append("Output Logs: ").append(outputLogs.size()).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Get execution report with all outputs and state.
     */
    public String getExecutionReport() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("```\n");
        
        if (!outputLogs.isEmpty()) {
            sb.append("=== Execution Logs ===\n");
            for (String log : outputLogs) {
                sb.append(log).append("\n");
            }
            sb.append("\n");
        }
        
        if (!messageHistory.isEmpty()) {
            sb.append("=== Messages ===\n");
            for (String msg : messageHistory) {
                sb.append(msg).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("=== Final State ===\n");
        sb.append("Players: ").append(players.size()).append("\n");
        for (MockPlayer player : players.values()) {
            sb.append("  ").append(player.getName())
              .append(" | Health: ").append(String.format("%.1f", player.getHealth()))
              .append(" | Location: (").append(String.format("%.1f", player.getX()))
              .append(", ").append(String.format("%.1f", player.getY()))
              .append(", ").append(String.format("%.1f", player.getZ()))
              .append(")\n");
        }
        
        sb.append("```");
        
        return sb.toString();
    }
    
    // === Simulated Event ===
    
    /**
     * Represents a queued event to be processed.
     */
    public static class SimulatedEvent {
        private final String eventName;
        private final Map<String, QuillValue> context;
        
        public SimulatedEvent(String eventName, Map<String, QuillValue> context) {
            this.eventName = eventName;
            this.context = context;
        }
        
        public String getEventName() {
            return eventName;
        }
        
        public Map<String, QuillValue> getContext() {
            return context;
        }
        
        @Override
        public String toString() {
            return "Event(" + eventName + ")";
        }
    }
    
    // === Mock Classes ===
    
    /**
     * Mock player for simulation.
     */
    public static class MockPlayer {
        private String name;
        private double x, y, z;
        private double health;
        private double maxHealth;
        private int hunger;
        private Map<String, Object> inventory;
        private boolean flying;
        private String gamemode;
        
        public MockPlayer(String name, double x, double y, double z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.health = 20.0;
            this.maxHealth = 20.0;
            this.hunger = 20;
            this.inventory = new HashMap<>();
            this.flying = false;
            this.gamemode = "survival";
        }
        
        public String getName() { return name; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public double getHealth() { return health; }
        public double getMaxHealth() { return maxHealth; }
        public int getHunger() { return hunger; }
        public boolean isFlying() { return flying; }
        public String getGamemode() { return gamemode; }
        public Map<String, Object> getInventory() { return inventory; }
        
        public void setX(double x) { this.x = x; }
        public void setY(double y) { this.y = y; }
        public void setZ(double z) { this.z = z; }
        public void setHealth(double health) { this.health = Math.max(0, Math.min(health, maxHealth)); }
        public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }
        public void setHunger(int hunger) { this.hunger = Math.max(0, Math.min(hunger, 20)); }
        public void setFlying(boolean flying) { this.flying = flying; }
        public void setGamemode(String gamemode) { this.gamemode = gamemode; }
        
        public void teleport(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public void damage(double amount) {
            setHealth(health - amount);
        }
        
        public void heal(double amount) {
            setHealth(health + amount);
        }
        
        public void giveItem(String itemId, int amount) {
            inventory.put(itemId, (Integer) inventory.getOrDefault(itemId, 0) + amount);
        }
        
        public void removeItem(String itemId, int amount) {
            int current = (Integer) inventory.getOrDefault(itemId, 0);
            if (current > amount) {
                inventory.put(itemId, current - amount);
            } else {
                inventory.remove(itemId);
            }
        }
        
        @Override
        public String toString() {
            return String.format("Player(%s | Health: %.1f | Pos: %.1f, %.1f, %.1f)",
                name, health, x, y, z);
        }
    }
    
    /**
     * Mock world for simulation.
     */
    public static class MockWorld {
        private String name;
        private long time;
        private String weather;
        private Map<String, Object> blocks;
        
        public MockWorld(String name) {
            this.name = name;
            this.time = 0;
            this.weather = "clear";
            this.blocks = new HashMap<>();
        }
        
        public String getName() { return name; }
        public long getTime() { return time; }
        public String getWeather() { return weather; }
        
        public void setTime(long time) { this.time = time % 24000; }
        public void setWeather(String weather) { this.weather = weather; }
        
        public void setBlock(double x, double y, double z, String blockType) {
            String key = String.format("%.0f,%.0f,%.0f", x, y, z);
            blocks.put(key, blockType);
        }
        
        public String getBlock(double x, double y, double z) {
            String key = String.format("%.0f,%.0f,%.0f", x, y, z);
            return (String) blocks.getOrDefault(key, "air");
        }
        
        @Override
        public String toString() {
            return String.format("World(%s | Time: %d | Weather: %s)",
                name, time, weather);
        }
    }
}