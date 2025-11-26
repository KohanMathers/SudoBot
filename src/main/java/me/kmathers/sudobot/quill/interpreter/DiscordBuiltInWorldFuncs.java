package me.kmathers.sudobot.quill.interpreter;

import me.kmathers.sudobot.quill.simulation.DiscordSimulationContext;
import me.kmathers.sudobot.quill.simulation.DiscordSimulationContext.MockWorld;
import me.kmathers.sudobot.quill.interpreter.QuillValue.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Discord-compatible built-in world functions.
 * Provides world manipulation operations for Quill scripts.
 */
public class DiscordBuiltInWorldFuncs {
    
    public static class SetBlockFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SetBlockFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 4) {
                throw new RuntimeException("set_block() requires 4 arguments, got " + args.size());
            }

            double x = args.get(0).asNumber();
            double y = args.get(1).asNumber();
            double z = args.get(2).asNumber();
            String blockType = args.get(3).asString();

            MockWorld world = context.getDefaultWorld();
            world.setBlock(x, y, z, blockType);
            context.log("[BLOCK] Set block at (" + x + ", " + y + ", " + z + ") to " + blockType);

            return new BooleanValue(true);
        }
    }

    public static class GetBlockFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public GetBlockFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 3) {
                throw new RuntimeException("get_block() requires 3 arguments, got " + args.size());
            }

            double x = args.get(0).asNumber();
            double y = args.get(1).asNumber();
            double z = args.get(2).asNumber();

            MockWorld world = context.getDefaultWorld();
            String blockType = world.getBlock(x, y, z);

            return new StringValue(blockType);
        }
    }

    public static class BreakBlockFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public BreakBlockFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 3) {
                throw new RuntimeException("break_block() requires 3 arguments, got " + args.size());
            }

            double x = args.get(0).asNumber();
            double y = args.get(1).asNumber();
            double z = args.get(2).asNumber();

            MockWorld world = context.getDefaultWorld();
            world.setBlock(x, y, z, "air");
            context.log("[BREAK] Broke block at (" + x + ", " + y + ", " + z + ")");

            return new BooleanValue(true);
        }
    }

    public static class SetTimeFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SetTimeFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("set_time() requires 1 argument, got " + args.size());
            }

            long time = (long) args.get(0).asNumber();

            if (time < 0 || time > 24000) {
                throw new RuntimeException("set_time() expects time between 0 and 24000, got " + time);
            }

            MockWorld world = context.getDefaultWorld();
            world.setTime(time);
            context.log("[TIME] World time set to " + time);

            return new BooleanValue(true);
        }
    }

    public static class GetTimeFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public GetTimeFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 0) {
                throw new RuntimeException("get_time() requires 0 arguments, got " + args.size());
            }

            MockWorld world = context.getDefaultWorld();
            return new NumberValue(world.getTime());
        }
    }

    public static class SetWeatherFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SetWeatherFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("set_weather() requires 1 argument, got " + args.size());
            }

            String weather = args.get(0).asString().toLowerCase();

            java.util.Set<String> validWeather = java.util.Set.of("clear", "rain", "thunder");
            if (!validWeather.contains(weather)) {
                throw new RuntimeException("set_weather() expects one of [clear, rain, thunder], got " + weather);
            }

            MockWorld world = context.getDefaultWorld();
            world.setWeather(weather);
            context.log("[WEATHER] Weather set to " + weather);

            return new BooleanValue(true);
        }
    }

    public static class GetWeatherFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public GetWeatherFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 0) {
                throw new RuntimeException("get_weather() requires 0 arguments, got " + args.size());
            }

            MockWorld world = context.getDefaultWorld();
            return new StringValue(world.getWeather());
        }
    }

    public static class BroadcastFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public BroadcastFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("broadcast() requires 1 argument, got " + args.size());
            }

            String message = args.get(0).asString();
            context.log("[BROADCAST] " + message);

            for (DiscordSimulationContext.MockPlayer player : context.getAllPlayers()) {
                context.addMessage(player.getName(), message);
            }

            return new BooleanValue(true);
        }
    }

    public static class CreateWorldFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public CreateWorldFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("create_world() requires 1 argument, got " + args.size());
            }

            String worldName = args.get(0).asString();
            MockWorld world = context.createWorld(worldName);

            return new WorldValue(world);
        }
    }

    public static class GetWorldFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public GetWorldFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() < 0 || args.size() > 1) {
                throw new RuntimeException("get_world() requires 0 or 1 arguments, got " + args.size());
            }

            String worldName = args.isEmpty() ? "world" : args.get(0).asString();
            MockWorld world = context.getWorld(worldName);

            return new WorldValue(world);
        }
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
        
        public MockWorld getWorld() { return world; }
        
        @Override
        public String toString() { return "World(" + world.getName() + ")"; }
    }

    public static class SpawnEntityFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SpawnEntityFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 4) {
                throw new RuntimeException("spawn_entity() requires 4 arguments, got " + args.size());
            }

            String entityType = args.get(0).asString();
            double x = args.get(1).asNumber();
            double y = args.get(2).asNumber();
            double z = args.get(3).asNumber();

            context.log("[ENTITY] Spawned " + entityType + " at (" + x + ", " + y + ", " + z + ")");

            Map<String, Object> entityData = new HashMap<>();
            entityData.put("type", entityType);
            entityData.put("x", x);
            entityData.put("y", y);
            entityData.put("z", z);

            return new MapValue(new HashMap<>());
        }
    }

    public static class DistanceFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("distance() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer() || !args.get(1).isPlayer()) {
                throw new RuntimeException("distance() expects two players");
            }

            DiscordSimulationContext.MockPlayer p1 = (DiscordSimulationContext.MockPlayer) args.get(0).getValue();
            DiscordSimulationContext.MockPlayer p2 = (DiscordSimulationContext.MockPlayer) args.get(1).getValue();

            double dx = p1.getX() - p2.getX();
            double dy = p1.getY() - p2.getY();
            double dz = p1.getZ() - p2.getZ();

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            return new NumberValue(distance);
        }
    }
}