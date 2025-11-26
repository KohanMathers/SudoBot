package me.kmathers.sudobot.quill.mocks;

import java.util.Objects;

public class MockLocation {
    private MockWorld world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    
    public MockLocation(MockWorld world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
    }
    
    public MockLocation(MockWorld world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public MockWorld getWorld() {
        return world;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public int getBlockX() {
        return (int) Math.floor(x);
    }
    
    public int getBlockY() {
        return (int) Math.floor(y);
    }
    
    public int getBlockZ() {
        return (int) Math.floor(z);
    }
    
    public void setWorld(MockWorld world) {
        this.world = world;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public void setZ(double z) {
        this.z = z;
    }
    
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
    
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
    
    public double distance(MockLocation other) {
        if (other == null || !this.world.getName().equals(other.world.getName())) {
            return Double.MAX_VALUE;
        }
        
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    public double distanceSquared(MockLocation other) {
        if (other == null || !this.world.getName().equals(other.world.getName())) {
            return Double.MAX_VALUE;
        }
        
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        
        return dx * dx + dy * dy + dz * dz;
    }
    
    public MockLocation add(double x, double y, double z) {
        return new MockLocation(this.world, this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }
    
    public MockLocation subtract(double x, double y, double z) {
        return new MockLocation(this.world, this.x - x, this.y - y, this.z - z, this.yaw, this.pitch);
    }
    
    public MockLocation clone() {
        return new MockLocation(this.world, this.x, this.y, this.z, this.yaw, this.pitch);
    }
    
    public String getBlock() {
        return world.getBlockAt(this);
    }
    
    public void setBlock(String blockType) {
        world.setBlockAt(this, blockType);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockLocation that = (MockLocation) o;
        return getBlockX() == that.getBlockX() &&
               getBlockY() == that.getBlockY() &&
               getBlockZ() == that.getBlockZ() &&
               Objects.equals(world.getName(), that.world.getName());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(world.getName(), getBlockX(), getBlockY(), getBlockZ());
    }
    
    @Override
    public String toString() {
        return String.format("(%.1f, %.1f, %.1f) in %s", x, y, z, world.getName());
    }
    
    public String toBlockString() {
        return String.format("(%d, %d, %d) in %s", getBlockX(), getBlockY(), getBlockZ(), world.getName());
    }
}