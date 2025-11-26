package me.kmathers.sudobot.quill.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockWorld {
    private final String name;
    private long time;
    private boolean isThundering;
    private boolean hasStorm;
    private int weatherDuration;
    private final Map<MockLocation, String> blocks;
    private final List<MockEntity> entities;
    
    public MockWorld(String name) {
        this.name = name;
        this.time = 0;
        this.isThundering = false;
        this.hasStorm = false;
        this.weatherDuration = 0;
        this.blocks = new HashMap<>();
        this.entities = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public long getTime() {
        return time;
    }
    
    public boolean isThundering() {
        return isThundering;
    }
    
    public boolean hasStorm() {
        return hasStorm;
    }
    
    public int getWeatherDuration() {
        return weatherDuration;
    }
    
    public void setTime(long time) {
        this.time = time % 24000;
    }
    
    public void setThundering(boolean thundering) {
        this.isThundering = thundering;
        if (thundering) {
            this.hasStorm = true;
        }
    }
    
    public void setStorm(boolean storm) {
        this.hasStorm = storm;
        if (!storm) {
            this.isThundering = false;
        }
    }
    
    public void setWeatherDuration(int duration) {
        this.weatherDuration = duration;
    }
    
    public String getWeatherState() {
        if (isThundering) {
            return "thunder";
        } else if (hasStorm) {
            return "rain";
        } else {
            return "clear";
        }
    }
    
    public void setWeather(String weather, int duration) {
        this.weatherDuration = duration;
        switch (weather.toLowerCase()) {
            case "clear":
                this.hasStorm = false;
                this.isThundering = false;
                break;
            case "rain":
                this.hasStorm = true;
                this.isThundering = false;
                break;
            case "thunder":
                this.hasStorm = true;
                this.isThundering = true;
                break;
        }
    }
    
    public String getBlockAt(MockLocation location) {
        return blocks.getOrDefault(location, "air");
    }
    
    public void setBlockAt(MockLocation location, String blockType) {
        if ("air".equalsIgnoreCase(blockType)) {
            blocks.remove(location);
        } else {
            blocks.put(location, blockType.toLowerCase());
        }
    }
    
    public void breakBlock(MockLocation location) {
        blocks.remove(location);
    }
    
    public Map<MockLocation, String> getAllBlocks() {
        return new HashMap<>(blocks);
    }
    
    public MockEntity spawnEntity(MockLocation location, String entityType) {
        MockEntity entity = new MockEntity(entityType, location);
        entities.add(entity);
        return entity;
    }
    
    public void removeEntity(MockEntity entity) {
        entities.remove(entity);
        entity.setDead(true);
    }
    
    public List<MockEntity> getEntities() {
        return new ArrayList<>(entities);
    }
    
    public void strikeLightning(MockLocation location) {
        // In simulation, just log that lightning struck
    }
    
    public void createExplosion(MockLocation location, float power, boolean setFire) {
        // In simulation, just log the explosion
    }
    
    private final List<DroppedItem> droppedItems = new ArrayList<>();
    
    public void dropItem(MockLocation location, MockItemStack item) {
        droppedItems.add(new DroppedItem(location, item));
    }
    
    public List<DroppedItem> getDroppedItems() {
        return new ArrayList<>(droppedItems);
    }
    
    public static class DroppedItem {
        private final MockLocation location;
        private final MockItemStack item;
        
        public DroppedItem(MockLocation location, MockItemStack item) {
            this.location = location;
            this.item = item;
        }
        
        public MockLocation getLocation() {
            return location;
        }
        
        public MockItemStack getItem() {
            return item;
        }
        
        @Override
        public String toString() {
            return item + " at " + location;
        }
    }
    
    @Override
    public String toString() {
        return String.format("MockWorld{name='%s', time=%d, weather=%s}", 
                name, time, getWeatherState());
    }
    
    public String getDetailedState() {
        StringBuilder sb = new StringBuilder();
        sb.append("World: ").append(name).append("\n");
        sb.append("  Time: ").append(time).append(" (").append(getTimeOfDay()).append(")\n");
        sb.append("  Weather: ").append(getWeatherState());
        if (weatherDuration > 0) {
            sb.append(" (").append(weatherDuration).append(" ticks remaining)");
        }
        sb.append("\n");
        
        if (!blocks.isEmpty()) {
            sb.append("  Blocks:\n");
            int count = 0;
            for (Map.Entry<MockLocation, String> entry : blocks.entrySet()) {
                if (count++ < 10) {
                    sb.append("    ").append(entry.getValue())
                      .append(" at ").append(entry.getKey()).append("\n");
                }
            }
            if (blocks.size() > 10) {
                sb.append("    ... and ").append(blocks.size() - 10).append(" more\n");
            }
        }
        
        if (!entities.isEmpty()) {
            sb.append("  Entities: ").append(entities.size()).append("\n");
            for (MockEntity entity : entities) {
                if (!entity.isDead()) {
                    sb.append("    - ").append(entity).append("\n");
                }
            }
        }
        
        if (!droppedItems.isEmpty()) {
            sb.append("  Dropped Items:\n");
            for (DroppedItem item : droppedItems) {
                sb.append("    - ").append(item).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    private String getTimeOfDay() {
        if (time >= 0 && time < 6000) {
            return "Day";
        } else if (time >= 6000 && time < 12000) {
            return "Afternoon";
        } else if (time >= 12000 && time < 13800) {
            return "Dusk";
        } else if (time >= 13800 && time < 22200) {
            return "Night";
        } else {
            return "Dawn";
        }
    }
}