package me.kmathers.sudobot.quill.interpreter;

import me.kmathers.sudobot.quill.mocks.MockLocation;
import me.kmathers.sudobot.quill.mocks.MockEntity;
import me.kmathers.sudobot.quill.mocks.MockEvent;
import me.kmathers.sudobot.quill.simulation.DiscordSimulationContext.MockPlayer;
import me.kmathers.sudobot.quill.mocks.MockItemStack;

import me.kmathers.sudobot.quill.mocks.MockWorld;
import me.kmathers.sudobot.quill.parser.AST.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public abstract class QuillValue {    
    public enum ValueType {
        NUMBER,
        STRING,
        BOOLEAN,
        NULL,
        PLAYER,
        LOCATION,
        ITEM,
        SCOPE,
        LIST,
        ENTITY,
        FUNCTION,
        WORLD, 
        REGION,
        MAP,
        EVENT,
        INVENTORY,
    }
    
    public abstract ValueType getType();
    public abstract Object getValue();
    
    // === Type Checking ===
    
    public boolean isNumber() { return getType() == ValueType.NUMBER; }
    public boolean isString() { return getType() == ValueType.STRING; }
    public boolean isBoolean() { return getType() == ValueType.BOOLEAN; }
    public boolean isNull() { return getType() == ValueType.NULL; }
    public boolean isPlayer() { return getType() == ValueType.PLAYER; }
    public boolean isLocation() { return getType() == ValueType.LOCATION; }
    public boolean isItem() { return getType() == ValueType.ITEM; }
    public boolean isScope() { return getType() == ValueType.SCOPE; }
    public boolean isList() { return getType() == ValueType.LIST; }
    public boolean isEntity() { return getType() == ValueType.ENTITY; }
    public boolean isFunction() { return getType() == ValueType.FUNCTION; }
    public boolean isWorld() { return getType() == ValueType.WORLD; }
    public boolean isRegion() {return getType() == ValueType.REGION; }
    public boolean isMap() { return getType() == ValueType.MAP; }
    public boolean isEvent() { return getType() == ValueType.EVENT; }
    public boolean isInventory() { return getType() == ValueType.INVENTORY; }

    // === Type Conversion (with runtime checks) ===
    
    public double asNumber() {
        if (!isNumber()) {
            throw new RuntimeException("Expected number but got " + getType());
        }
        return (double) getValue();
    }
    
    public String asString() {
        if (!isString()) {
            throw new RuntimeException("Expected string but got " + getType());
        }
        return (String) getValue();
    }
    
    public boolean asBoolean() {
        if (!isBoolean()) {
            throw new RuntimeException("Expected boolean but got " + getType());
        }
        return (boolean) getValue();
    }
    
    public MockPlayer asPlayer() {
        if (!isPlayer()) {
            throw new RuntimeException("Expected player but got " + getType());
        }
        return (MockPlayer) getValue();
    }
    
    public MockLocation asLocation() {
        if (!isLocation()) {
            throw new RuntimeException("Expected location but got " + getType());
        }
        return (MockLocation) getValue();
    }
    
    public MockItemStack asItem() {
        if (!isItem()) {
            throw new RuntimeException("Expected item but got " + getType());
        }
        return (MockItemStack) getValue();
    }
    
    public ScopeValue asScope() {
        if (!isScope()) {
            throw new RuntimeException("Expected sock but got " + getType());
        }
        return (ScopeValue) this;
    }
    
    @SuppressWarnings("unchecked")
    public List<QuillValue> asList() {
        if (!isList()) {
            throw new RuntimeException("Expected list but got " + getType());
        }
        return (List<QuillValue>) getValue();
    }
    
    public MockEntity asEntity() {
        if (!isEntity()) {
            throw new RuntimeException("Expected entity but got " + getType());
        }
        return (MockEntity) getValue();
    }
    
    public MockWorld asWorld() {
        if (!isWorld()) {
            throw new RuntimeException("Expected world but got " + getType());
        }
        return (MockWorld) getValue();
    }
    
    public RegionValue asRegion() {
        if (!isRegion()) {
            throw new RuntimeException("Expected region but got " + getType());
        }
        return (RegionValue) getValue();
    }

    @SuppressWarnings("unchecked")
    public Map<String, QuillValue> asMap() {
        if (!isMap()) {
            throw new RuntimeException("Expected map but got " + getType());
        }
        return (Map<String, QuillValue>) getValue();
    }
    
    public MockEvent asEvent() {
        if (!isEvent()) {
            throw new RuntimeException("Expected event but got " + getType());
        }
        return (MockEvent) getValue();
    }

    public InventoryValue asInventory() {
        if (!isInventory()) {
            throw new RuntimeException("Expected inventory but got " + getType());
        }
        return (InventoryValue) getValue();
    }

    // === Truthiness ===
    
    public boolean isTruthy() {
        if (isNull()) return false;
        if (isBoolean()) return asBoolean();
        if (isNumber()) return asNumber() != 0;
        if (isString()) return !asString().isEmpty();
        if (isList()) return !asList().isEmpty();
        return true;
    }
    
    // === String Representation ===
    
    @Override
    public String toString() {
        if (isNull()) return "null";
        if (isString()) return asString();
        return String.valueOf(getValue());
    }
    
    // === Concrete Value Types ===
    
    public static class NumberValue extends QuillValue {
        private final double value;
        
        public NumberValue(double value) {
            this.value = value;
        }
        
        @Override
        public ValueType getType() { return ValueType.NUMBER; }
        
        @Override
        public Object getValue() { return value; }
        
        @Override
        public String toString() { 
            if (value == Math.floor(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
    }
    
    public static class StringValue extends QuillValue {
        private final String value;
        
        public StringValue(String value) {
            this.value = value;
        }
        
        @Override
        public ValueType getType() { return ValueType.STRING; }
        
        @Override
        public Object getValue() { return value; }
    }
    
    public static class BooleanValue extends QuillValue {
        private final boolean value;
        
        public BooleanValue(boolean value) {
            this.value = value;
        }
        
        @Override
        public ValueType getType() { return ValueType.BOOLEAN; }
        
        @Override
        public Object getValue() { return value; }
        
        @Override
        public String toString() { return String.valueOf(value); }
    }
    
    public static class NullValue extends QuillValue {
        public static final NullValue INSTANCE = new NullValue();
        
        private NullValue() {}
        
        @Override
        public ValueType getType() { return ValueType.NULL; }
        
        @Override
        public Object getValue() { return null; }
        
        @Override
        public String toString() { return "null"; }
    }
    
    public static class PlayerValue extends QuillValue {
        private final MockPlayer player;
        
        public PlayerValue(MockPlayer player) {
            this.player = player;
        }
        
        @Override
        public ValueType getType() { return ValueType.PLAYER; }
        
        @Override
        public Object getValue() { return player; }
        
        @Override
        public String toString() { return "Player(" + player.getName() + ")"; }
    }
    
    public static class LocationValue extends QuillValue {
        private final MockLocation location;
        
        public LocationValue(MockLocation location) {
            this.location = location;
        }
        
        @Override
        public ValueType getType() { return ValueType.LOCATION; }
        
        @Override
        public Object getValue() { return location; }
        
        @Override
        public String toString() { 
            return String.format("Location(%.1f, %.1f, %.1f)", 
                location.getX(), location.getY(), location.getZ());
        }
    }
    
    public static class ItemValue extends QuillValue {
        private final MockItemStack item;
        
        public ItemValue(MockItemStack item) {
            this.item = item;
        }
        
        @Override
        public ValueType getType() { return ValueType.ITEM; }
        
        @Override
        public Object getValue() { return item; }
        
        @Override
        public String toString() { 
            return "Item(" + item.getType() + " x" + item.getAmount() + ")";
        }
    }
    
    public static class ScopeValue extends QuillValue {
        private final ScopeContext scope;
        
        public ScopeValue(ScopeContext scope) {
            this.scope = scope;
        }
        
        @Override
        public ValueType getType() { return ValueType.SCOPE; }
        
        @Override
        public Object getValue() { return scope; }
        
        public ScopeContext getScope() { return scope; }
        
        @Override
        public String toString() { return "Scope(" + scope.getName() + ")"; }
    }
    
    public static class ListValue extends QuillValue {
        private final List<QuillValue> elements;
        
        public ListValue(List<QuillValue> elements) {
            this.elements = elements;
        }
        
        @Override
        public ValueType getType() { return ValueType.LIST; }
        
        @Override
        public Object getValue() { return elements; }
        
        @Override
        public String toString() { 
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < elements.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(elements.get(i).toString());
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    public static class EntityValue extends QuillValue {
        private final MockEntity entity;
        
        public EntityValue(MockEntity entity) {
            this.entity = entity;
        }
        
        @Override
        public ValueType getType() { return ValueType.ENTITY; }
        
        @Override
        public Object getValue() { return entity; }
        
        @Override
        public String toString() { 
            return "Entity(" + entity.getType() + ")";
        }
    }
    
    public static class FunctionValue extends QuillValue {
        private final String name;
        private final List<String> parameters;
        private final ASTNode body;
        private final ScopeContext closure;
        
        public FunctionValue(String name, List<String> parameters, 
                           ASTNode body, 
                           ScopeContext closure) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
            this.closure = closure;
        }
        
        @Override
        public ValueType getType() { return ValueType.FUNCTION; }
        
        @Override
        public Object getValue() { return this; }
        
        public String getName() { return name; }
        public List<String> getParameters() { return parameters; }
        public ASTNode getBody() { return body; }
        public ScopeContext getClosure() { return closure; }
        
        @Override
        public String toString() { return "Function(" + name + ")"; }
    }
    
    public static class WorldValue extends QuillValue {
        private final MockWorld world;
        
        public WorldValue(MockWorld world) {
            this.world = world;
        }
        
        @Override
        public ValueType getType() { return ValueType.WORLD; }
        
        @Override
        public Object getValue() { return world; }
        
        @Override
        public String toString() { return "World(" + world.getName() + ")"; }
    }

    public static class RegionValue extends QuillValue {
        private final double x1, y1, z1, x2, y2, z2;
        
        public RegionValue(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
        }
        
        @Override
        public ValueType getType() { return ValueType.REGION; }
        
        @Override
        public Object getValue() { return this; }
        
        public double getX1() { return x1; }
        public double getY1() { return y1; }
        public double getZ1() { return z1; }
        public double getX2() { return x2; }
        public double getY2() { return y2; }
        public double getZ2() { return z2; }
        
        @Override
        public String toString() { 
            return String.format("Region(%.1f, %.1f, %.1f -> %.1f, %.1f, %.1f)", 
                x1, y1, z1, x2, y2, z2);
        }
    }

    public static class InventoryValue extends QuillValue {
        private List<MockItemStack> inventory = new ArrayList<>();
        private boolean large;
        private final int SMALL_SIZE = 27;
        private final int LARGE_SIZE = 52;
        private String name;

        public InventoryValue(String name, boolean large) {
            String air = "air";
            this.large = large;
            if (large) {
                this.inventory = IntStream.range(0, 54).mapToObj(i -> new MockItemStack(air)).toList();
            } else {
                this.inventory = IntStream.range(0, 27).mapToObj(i -> new MockItemStack(air)).toList();
            }
            this.name = name;
        }

        @Override
        public ValueType getType() { return ValueType.INVENTORY; }
        
        @Override
        public Object getValue() { return this; }

        public MockItemStack getSlot(int slot) {
            if (large) {
                if (slot > LARGE_SIZE || slot < 0) {
                    throw new RuntimeException("Invalid index " + slot + " in " + name);
                }
            } else {
                if (slot > SMALL_SIZE || slot < 0) {
                    throw new RuntimeException("Invalid index " + slot + " in " + name);
                }
            }
            return (inventory.get(slot)); 
        }

        public void setSlot(int slot, MockItemStack item) {
            if (large) {
                if (slot > LARGE_SIZE || slot < 0) {
                    throw new RuntimeException("Invalid index " + slot + " in " + name);
                }
            } else {
                if (slot > SMALL_SIZE || slot < 0) {
                    throw new RuntimeException("Invalid index " + slot + " in " + name);
                }
            }
            inventory.set(slot, item);
        }

        public String getName() { return this.name; }

        public void setName(String name) { this.name = name; };

        public int getSize() { return this.large ? LARGE_SIZE : SMALL_SIZE; }

        public List<MockItemStack> getInventory() { return this.inventory; }

        public boolean isLarge() { return this.large; }

        public void clear() {
            String air = "air";
            if (large) {
                this.inventory = IntStream.range(0, 27).mapToObj(i -> new MockItemStack(air)).toList();
            } else {
                this.inventory = IntStream.range(0, 54).mapToObj(i -> new MockItemStack(air)).toList();
            }
        }

        public void clearSlot(int slot) {
            if (large) {
                if (slot > LARGE_SIZE || slot < 0) {
                    throw new RuntimeException("Invalid index " + slot + " in " + name);
                }
            } else {
                if (slot > SMALL_SIZE || slot < 0) {
                    throw new RuntimeException("Invalid index " + slot + " in " + name);
                }
            }
            inventory.set(slot, new MockItemStack("air"));
        }

        public int addItem(MockItemStack item) {
            for (int index = 0; index < inventory.size(); index++) {
                if (inventory.get(index).isSimilar(new MockItemStack("air"))) {
                    inventory.set(index, item);
                    return index;
                }
            }
            return -1;
        }

        public boolean removeItem(MockItemStack item) {
            for (int index = 0; index < inventory.size(); index++) {
                if (inventory.get(index).isSimilar(item)) {
                    inventory.set(index, new MockItemStack("air"));
                    return true;
                }
            }
            return false;
        }

        public boolean contains(MockItemStack item) {
            for (int index = 0; index < inventory.size(); index++) {
                if (inventory.get(index).isSimilar(item)) {
                    return true;
                }
            }
            return false;
        }

        public boolean containsAtLeast(MockItemStack item, int amount) {
            int realAmount = 0;
            for (int index = 0; index < inventory.size(); index++) {
                if (inventory.get(index).isSimilar(item)) {
                    realAmount = realAmount + inventory.get(index).getAmount();
                }
            }
            return realAmount >= amount;
        }

        public int getAmount(MockItemStack item) {
            int amount = 0;
            for (int index = 0; index < inventory.size(); index++) {
                if (inventory.get(index).isSimilar(item)) {
                    amount = amount + inventory.get(index).getAmount();
                }
            }
            return amount;
        }

        public List<MockItemStack> getAllItems() {
            List<MockItemStack> items = new ArrayList<>();
            for (int index = 0; index < inventory.size(); index++) {
                if (!(inventory.get(index).isSimilar(new MockItemStack("air")))) {
                    items.add(inventory.get(index));
                }
            }
            return items;
        }

        public void setInventory(List<MockItemStack> items) {
            int size = items.size();
            if (large) {
                if (size > LARGE_SIZE || size < 0) {
                    throw new RuntimeException("Invalid inventory size " + size + " for " + name);
                }
            } else {
                if (size > SMALL_SIZE || size < 0) {
                    throw new RuntimeException("Invalid inventory size " + size + " for " + name);
                }
            }
            inventory = items;
        }

        public boolean isEmpty() {
            for (int index = 0; index < inventory.size(); index++) {
                if (!(inventory.get(index).isSimilar(new MockItemStack("air")))) {
                    return false;
                }
            }
            return true;
        }

        public boolean isFull() {
            for (int index = 0; index < inventory.size(); index++) {
                if (inventory.get(index).isSimilar(new MockItemStack("air"))) {
                    return false;
                }
            }
            return true;
        }

        public int firstEmpty() {
            for (int index = 0; index < inventory.size(); index++) {
                if (inventory.get(index).isSimilar(new MockItemStack("air"))) {
                    return index;
                }
            }
            return -1;
        }

        public List<Integer> all(MockItemStack item) {
            List<Integer> all = new ArrayList<>();
            for (int index = 0; index < inventory.size(); index++) {
                if (inventory.get(index).isSimilar(item)) {
                    all.add(index);
                }
            }
            return all;
        }
    }

    public static class MapValue extends QuillValue {
        private final Map<String, QuillValue> map;
        
        public MapValue(Map<String, QuillValue> map) {
            this.map = map;
        }
        
        @Override
        public ValueType getType() { return ValueType.MAP; }
        
        @Override
        public Object getValue() { return map; }
        
        public QuillValue get(String key) {
            return map.getOrDefault(key, NullValue.INSTANCE);
        }
        
        public void put(String key, QuillValue value) {
            map.put(key, value);
        }
        
        public boolean has(String key) {
            return map.containsKey(key);
        }
        
        public Map<String, QuillValue> getMap() {
            return map;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, QuillValue> entry : map.entrySet()) {
                if (i > 0) sb.append(", ");
                sb.append(entry.getKey()).append(": ").append(entry.getValue().toString());
                i++;
            }
            sb.append("}");
            return sb.toString();
        }
    }
    
    public static class EventValue extends QuillValue {
        private final MockEvent event;
        
        public EventValue(MockEvent event) {
            this.event = event;
        }
        
        @Override
        public ValueType getType() { return ValueType.EVENT; }
        
        @Override
        public Object getValue() { return event; }
        
        public MockEvent getEvent() {
            return event;
        }
        
        @Override
        public String toString() { 
            return "Event(" + event.getEventName() + ")";
        }
    }
}