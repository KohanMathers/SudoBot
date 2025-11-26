package me.kmathers.sudobot.quill.mocks;

import java.util.UUID;

public class MockEntity {
    private final UUID uniqueId;
    private final String type;
    private MockLocation location;
    private boolean isDead;
    private String customName;
    private boolean customNameVisible;
    private double health;
    private double maxHealth;
    
    public MockEntity(String type, MockLocation location) {
        this.uniqueId = UUID.randomUUID();
        this.type = type.toLowerCase();
        this.location = location;
        this.isDead = false;
        this.customName = null;
        this.customNameVisible = false;
        this.health = getDefaultHealth(type);
        this.maxHealth = this.health;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public String getType() {
        return type;
    }
    
    public MockLocation getLocation() {
        return location;
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    public String getCustomName() {
        return customName;
    }
    
    public boolean isCustomNameVisible() {
        return customNameVisible;
    }
    
    public double getHealth() {
        return health;
    }
    
    public double getMaxHealth() {
        return maxHealth;
    }
    
    public void setLocation(MockLocation location) {
        this.location = location;
    }
    
    public void teleport(MockLocation location) {
        this.location = location;
    }
    
    public void setDead(boolean dead) {
        this.isDead = dead;
    }
    
    public void remove() {
        this.isDead = true;
    }
    
    public void setCustomName(String customName) {
        this.customName = customName;
    }
    
    public void setCustomNameVisible(boolean visible) {
        this.customNameVisible = visible;
    }
    
    public void setHealth(double health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
        if (this.health == 0) {
            this.isDead = true;
        }
    }
    
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
        if (this.health > maxHealth) {
            this.health = maxHealth;
        }
    }
    
    public void damage(double amount) {
        setHealth(health - amount);
    }
    
    public void heal(double amount) {
        setHealth(health + amount);
    }
    
    public boolean isPlayer() {
        return "player".equalsIgnoreCase(type);
    }
    
    public boolean isLiving() {
        return !isProjectile() && !isItem();
    }
    
    public boolean isProjectile() {
        return type.contains("arrow") || type.contains("snowball") || 
               type.contains("egg") || type.contains("fireball");
    }
    
    public boolean isItem() {
        return "item".equalsIgnoreCase(type) || "dropped_item".equalsIgnoreCase(type);
    }
    
    public boolean isHostile() {
        return type.equals("zombie") || type.equals("skeleton") || 
               type.equals("creeper") || type.equals("spider") ||
               type.equals("enderman") || type.equals("witch");
    }
    
    public boolean isPassive() {
        return type.equals("cow") || type.equals("pig") || 
               type.equals("sheep") || type.equals("chicken") ||
               type.equals("horse") || type.equals("villager");
    }
    
    private double getDefaultHealth(String type) {
        switch (type.toLowerCase()) {
            case "zombie":
            case "skeleton":
                return 20.0;
            case "creeper":
                return 20.0;
            case "spider":
                return 16.0;
            case "enderman":
                return 40.0;
            case "cow":
            case "pig":
            case "sheep":
                return 10.0;
            case "chicken":
                return 4.0;
            case "horse":
                return 30.0;
            case "villager":
                return 20.0;
            case "wolf":
                return 8.0;
            case "cat":
                return 10.0;
            case "iron_golem":
                return 100.0;
            default:
                return 20.0;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (customName != null && customNameVisible) {
            sb.append(customName).append(" (");
        }
        
        sb.append(formatType(type));
        
        if (customName != null && customNameVisible) {
            sb.append(")");
        }
        
        if (isDead) {
            sb.append(" [DEAD]");
        } else {
            sb.append(" [").append(String.format("%.1f/%.1f HP", health, maxHealth)).append("]");
        }
        
        sb.append(" at ").append(location);
        
        return sb.toString();
    }
    
    private String formatType(String type) {
        StringBuilder sb = new StringBuilder();
        String[] parts = type.split("_");
        for (String part : parts) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }
}