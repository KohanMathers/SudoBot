package me.kmathers.sudobot.quill.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockPlayer {
    private final String name;
    private final UUID uniqueId;
    private MockLocation location;
    private double health;
    private double maxHealth;
    private int foodLevel;
    private String gameMode;
    private boolean isFlying;
    private boolean allowFlight;
    private boolean isOp;
    private final List<MockItemStack> inventory;
    private final Map<String, MockPotionEffect> activeEffects;
    private MockWorld world;
    
    public MockPlayer(String name) {
        this.name = name;
        this.uniqueId = UUID.randomUUID();
        this.health = 20.0;
        this.maxHealth = 20.0;
        this.foodLevel = 20;
        this.gameMode = "survival";
        this.isFlying = false;
        this.allowFlight = false;
        this.isOp = false;
        this.inventory = new ArrayList<>();
        this.activeEffects = new HashMap<>();
        this.world = new MockWorld("world");
        this.location = new MockLocation(world, 0, 64, 0);
    }
    
    public MockPlayer(String name, MockWorld world, double x, double y, double z) {
        this.name = name;
        this.uniqueId = UUID.randomUUID();
        this.health = 20.0;
        this.maxHealth = 20.0;
        this.foodLevel = 20;
        this.gameMode = "survival";
        this.isFlying = false;
        this.allowFlight = false;
        this.isOp = false;
        this.inventory = new ArrayList<>();
        this.activeEffects = new HashMap<>();
        this.world = world;
        this.location = new MockLocation(world, x, y, z);
    }
    
    public String getName() {
        return name;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public MockLocation getLocation() {
        return location;
    }
    
    public MockWorld getWorld() {
        return world;
    }
    
    public double getHealth() {
        return health;
    }
    
    public double getMaxHealth() {
        return maxHealth;
    }
    
    public int getFoodLevel() {
        return foodLevel;
    }
    
    public String getGameMode() {
        return gameMode;
    }
    
    public boolean isFlying() {
        return isFlying;
    }
    
    public boolean getAllowFlight() {
        return allowFlight;
    }
    
    public boolean isOp() {
        return isOp;
    }
    
    public boolean isOnline() {
        return true;
    }
    
    public void setLocation(MockLocation location) {
        this.location = location;
        this.world = location.getWorld();
    }
    
    public void teleport(MockLocation location) {
        this.location = location;
        this.world = location.getWorld();
    }
    
    public void setHealth(double health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }
    
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
        if (this.health > maxHealth) {
            this.health = maxHealth;
        }
    }
    
    public void heal(double amount) {
        this.health = Math.min(maxHealth, health + amount);
    }
    
    public void damage(double amount) {
        this.health = Math.max(0, health - amount);
    }
    
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = Math.max(0, Math.min(20, foodLevel));
    }
    
    public void setGameMode(String gameMode) {
        this.gameMode = gameMode.toLowerCase();
    }
    
    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }
    
    public void setAllowFlight(boolean allowFlight) {
        this.allowFlight = allowFlight;
        if (!allowFlight) {
            this.isFlying = false;
        }
    }
    
    public void setOp(boolean op) {
        this.isOp = op;
    }
    
    public List<MockItemStack> getInventory() {
        return inventory;
    }
    
    public void giveItem(MockItemStack item) {
        for (MockItemStack existing : inventory) {
            if (existing.getType().equals(item.getType())) {
                existing.setAmount(existing.getAmount() + item.getAmount());
                return;
            }
        }
        inventory.add(item.clone());
    }
    
    public boolean hasItem(String materialType, int amount) {
        int count = 0;
        for (MockItemStack item : inventory) {
            if (item.getType().equalsIgnoreCase(materialType)) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }
    
    public boolean removeItem(String materialType, int amount) {
        if (!hasItem(materialType, amount)) {
            return false;
        }
        
        int remaining = amount;
        List<MockItemStack> toRemove = new ArrayList<>();
        
        for (MockItemStack item : inventory) {
            if (item.getType().equalsIgnoreCase(materialType)) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    toRemove.add(item);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
                
                if (remaining == 0) {
                    break;
                }
            }
        }
        
        inventory.removeAll(toRemove);
        return true;
    }
    
    public void clearInventory() {
        inventory.clear();
    }
    
    public void addPotionEffect(MockPotionEffect effect) {
        activeEffects.put(effect.getType(), effect);
    }
    
    public void removePotionEffect(String effectType) {
        activeEffects.remove(effectType.toLowerCase());
    }
    
    public void clearPotionEffects() {
        activeEffects.clear();
    }
    
    public boolean hasPotionEffect(String effectType) {
        return activeEffects.containsKey(effectType.toLowerCase());
    }
    
    public Map<String, MockPotionEffect> getActivePotionEffects() {
        return new HashMap<>(activeEffects);
    }
    
    private final List<String> messageHistory = new ArrayList<>();
    
    public void sendMessage(String message) {
        messageHistory.add(message);
    }
    
    public List<String> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
    
    public void clearMessageHistory() {
        messageHistory.clear();
    }
    
    @Override
    public String toString() {
        return String.format("MockPlayer{name='%s', health=%.1f/%.1f, hunger=%d, gamemode=%s, location=%s}",
                name, health, maxHealth, foodLevel, gameMode, location);
    }
    
    public String getDetailedState() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player: ").append(name).append("\n");
        sb.append("  Health: ").append(String.format("%.1f/%.1f", health, maxHealth)).append("\n");
        sb.append("  Hunger: ").append(foodLevel).append("/20\n");
        sb.append("  Gamemode: ").append(gameMode).append("\n");
        sb.append("  Location: ").append(location).append("\n");
        sb.append("  Flying: ").append(isFlying).append("\n");
        
        if (!inventory.isEmpty()) {
            sb.append("  Inventory:\n");
            for (MockItemStack item : inventory) {
                sb.append("    - ").append(item).append("\n");
            }
        }
        
        if (!activeEffects.isEmpty()) {
            sb.append("  Effects:\n");
            for (MockPotionEffect effect : activeEffects.values()) {
                sb.append("    - ").append(effect).append("\n");
            }
        }
        
        if (!messageHistory.isEmpty()) {
            sb.append("  Messages Received:\n");
            for (String msg : messageHistory) {
                sb.append("    - ").append(msg).append("\n");
            }
        }
        
        return sb.toString();
    }
}