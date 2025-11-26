package me.kmathers.sudobot.quill.mocks;

import java.util.HashMap;
import java.util.Map;

public class MockItemStack {
    private String type;
    private int amount;
    private final Map<String, Object> metadata;
    
    public MockItemStack(String type) {
        this(type, 1);
    }
    
    public MockItemStack(String type, int amount) {
        this.type = type.toLowerCase();
        this.amount = amount;
        this.metadata = new HashMap<>();
    }
    
    public String getType() {
        return type;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    public void setType(String type) {
        this.type = type.toLowerCase();
    }
    
    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
    }
    
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    public void clearMetadata() {
        metadata.clear();
    }
    
    public void setDisplayName(String displayName) {
        metadata.put("display_name", displayName);
    }
    
    public String getDisplayName() {
        return (String) metadata.getOrDefault("display_name", formatName(type));
    }
    
    public void setLore(String... lore) {
        metadata.put("lore", lore);
    }
    
    public String[] getLore() {
        return (String[]) metadata.getOrDefault("lore", new String[0]);
    }
    
    public boolean isSimilar(MockItemStack other) {
        if (other == null) return false;
        if (!this.type.equals(other.type)) return false;
        return this.metadata.equals(other.metadata);
    }
    
    public MockItemStack clone() {
        MockItemStack clone = new MockItemStack(this.type, this.amount);
        clone.metadata.putAll(this.metadata);
        return clone;
    }
    
    public boolean isEmpty() {
        return amount <= 0 || "air".equalsIgnoreCase(type);
    }
    
    public int getMaxStackSize() {
        if (isToolOrArmor(type)) {
            return 1;
        } else if (isLimitedStack(type)) {
            return 16;
        } else {
            return 64;
        }
    }
    
    private boolean isToolOrArmor(String type) {
        return type.contains("sword") || type.contains("pickaxe") || 
               type.contains("axe") || type.contains("shovel") ||
               type.contains("hoe") || type.contains("helmet") ||
               type.contains("chestplate") || type.contains("leggings") ||
               type.contains("boots");
    }
    
    private boolean isLimitedStack(String type) {
        return type.contains("ender_pearl") || type.contains("snowball") ||
               type.contains("egg") || type.contains("bucket");
    }
    
    private String formatName(String type) {
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (hasMetadata("display_name")) {
            sb.append(getDisplayName());
        } else {
            sb.append(formatName(type));
        }
        
        if (amount > 1) {
            sb.append(" x").append(amount);
        }
        
        return sb.toString();
    }
    
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString());
        
        if (!metadata.isEmpty()) {
            sb.append(" {");
            boolean first = true;
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            sb.append("}");
        }
        
        return sb.toString();
    }
}