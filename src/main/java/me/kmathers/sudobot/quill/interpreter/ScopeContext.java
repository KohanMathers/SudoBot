package me.kmathers.sudobot.quill.interpreter;

import java.util.*;

/**
 * Discord-compatible scope context.
 * Simplified version of ScopeContext without Bukkit dependencies.
 * Manages variable storage, scope hierarchy, and regions.
 */
public class ScopeContext {
    private final String name;
    private final ScopeContext parent;
    private final Map<String, QuillValue> variables;
    private final Set<String> consts;
    private final Map<String, ScopeContext> subscopes;
    private Region region;
    
    // Root scopes
    public ScopeContext(String name, Region region) {
        this.name = name;
        this.parent = null;
        this.variables = new HashMap<>();
        this.consts = new HashSet<>();
        this.subscopes = new HashMap<>();
        this.region = region;
    }
    
    // Subscopes
    public ScopeContext(String name, ScopeContext parent, Region region) {
        this.name = name;
        this.parent = parent;
        this.variables = new HashMap<>();
        this.consts = new HashSet<>();
        this.subscopes = new HashMap<>();
        this.region = region;
    }
    
    // Nested execution (functions, etc)
    public ScopeContext(String name, ScopeContext parent) {
        this.name = name;
        this.parent = parent;
        this.variables = new HashMap<>();
        this.consts = new HashSet<>();
        this.subscopes = new HashMap<>();
        this.region = parent != null ? parent.region : null;
    }
    
    // === Variable Management ===
    
    /**
     * Define a new variable in this scope.
     * Throws if variable already exists in this scope.
     */
    public void define(String name, QuillValue value) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("Variable '" + name + "' already defined in this scope");
        }
        variables.put(name, value);
    }
    
    /**
     * Define a constant in this scope.
     * Constants cannot be reassigned after definition.
     */
    public void defineConst(String name, QuillValue value) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("Variable '" + name + "' already defined in this scope");
        }
        variables.put(name, value);
        consts.add(name);
    }

    /**
     * Check if a variable is a constant.
     */
    private boolean isConst(String name) {
        if (consts.contains(name)) {
            return true;
        }
        if (parent != null && parent.has(name)) {
            return parent.isConst(name);
        }
        return false;
    }

    /**
     * Set a variable's value.
     * Looks up the scope chain to find where the variable is defined.
     * If not found anywhere, defines it in the current scope.
     */
    public void set(String name, QuillValue value) {
        if (consts.contains(name)) {
            throw new RuntimeException("Cannot reassign constant variable '" + name + "'");
        }

        if (variables.containsKey(name)) {
            variables.put(name, value);
            return;
        }
        
        if (parent != null) {
            try {
                parent.get(name);
                if (parent.isConst(name)) {
                    throw new RuntimeException("Cannot reassign constant variable '" + name + "'");
                }
                parent.set(name, value);
                return;
            } catch (RuntimeException e) {
                // If a const error, re-throw
                if (e.getMessage().contains("Cannot reassign constant")) {
                    throw e;
                }
                // Variable doesn't exist in parent chain, define it here
            }
        }
        
        variables.put(name, value);
    }
    
    /**
     * Get a variable's value.
     * Looks up the scope chain until found.
     */
    public QuillValue get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        
        if (parent != null) {
            return parent.get(name);
        }
        
        throw new RuntimeException("Undefined variable: '" + name + "'");
    }
    
    /**
     * Check if a variable exists in this scope or parent scopes.
     */
    public boolean has(String name) {
        if (variables.containsKey(name)) {
            return true;
        }
        if (parent != null) {
            return parent.has(name);
        }
        return false;
    }
    
    // === Subscope Management ===
    
    /**
     * Register a subscope with a name.
     */
    public void registerSubscope(String name, ScopeContext subscope) {
        subscopes.put(name, subscope);
    }
    
    /**
     * Get a subscope by name.
     */
    public ScopeContext getSubscope(String name) {
        return subscopes.get(name);
    }
    
    /**
     * Check if a subscope exists.
     */
    public boolean hasSubscope(String name) {
        return subscopes.containsKey(name);
    }
    
    // === Region Management ===
    
    /**
     * Get this scope's physical region.
     */
    public Region getRegion() {
        return region;
    }
    
    /**
     * Set this scope's physical region.
     */
    public void setRegion(Region region) {
        this.region = region;
    }
    
    // === Scope Hierarchy ===
    
    /**
     * Get the parent scope.
     */
    public ScopeContext getParent() {
        return parent;
    }
    
    /**
     * Get the root scope (topmost parent).
     */
    public ScopeContext getRoot() {
        ScopeContext current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }
    
    /**
     * Check if this is a root scope (no parent).
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    // === Getters ===
    
    public String getName() {
        return name;
    }
    
    public Map<String, QuillValue> getVariables() {
        return new HashMap<>(variables);
    }
    
    // === Debugging ===
    
    @Override
    public String toString() {
        return "Scope(" + name + ", vars=" + variables.size() + ")";
    }
    
    /**
     * Represents a 3D rectangular region.
     * Simplified version without world name requirement.
     */
    public static class Region {
        private final double x1, y1, z1;
        private final double x2, y2, z2;
        
        public Region(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.z1 = Math.min(z1, z2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.z2 = Math.max(z1, z2);
        }
        
        /**
         * Check if coordinates are within this region.
         */
        public boolean contains(double x, double y, double z) {
            return x >= x1 && x <= x2 &&
                   y >= y1 && y <= y2 &&
                   z >= z1 && z <= z2;
        }
        
        /**
         * Check if this region is fully contained within another region.
         */
        public boolean isWithin(Region other) {
            return x1 >= other.x1 && x2 <= other.x2 &&
                   y1 >= other.y1 && y2 <= other.y2 &&
                   z1 >= other.z1 && z2 <= other.z2;
        }
        
        /**
         * Check if this region overlaps with another region.
         */
        public boolean overlaps(Region other) {
            return x1 <= other.x2 && x2 >= other.x1 &&
                   y1 <= other.y2 && y2 >= other.y1 &&
                   z1 <= other.z2 && z2 >= other.z1;
        }
        
        public double getX1() { return x1; }
        public double getY1() { return y1; }
        public double getZ1() { return z1; }
        public double getX2() { return x2; }
        public double getY2() { return y2; }
        public double getZ2() { return z2; }
        
        @Override
        public String toString() {
            return String.format("Region(%.1f,%.1f,%.1f to %.1f,%.1f,%.1f)", 
                x1, y1, z1, x2, y2, z2);
        }
    }
}