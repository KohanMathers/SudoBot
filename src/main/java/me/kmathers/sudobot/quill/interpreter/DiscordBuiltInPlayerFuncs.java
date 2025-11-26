package me.kmathers.sudobot.quill.interpreter;

import me.kmathers.sudobot.quill.simulation.DiscordSimulationContext;
import me.kmathers.sudobot.quill.simulation.DiscordSimulationContext.MockPlayer;
import me.kmathers.sudobot.quill.interpreter.QuillValue.*;

import java.util.List;

/**
 * Discord-compatible built-in player functions.
 * Provides player manipulation operations for Quill scripts.
 */
public class DiscordBuiltInPlayerFuncs {
    public static class TeleportFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public TeleportFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2 && args.size() != 4) {
                throw new RuntimeException("teleport() requires 2 or 4 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("teleport() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();

            if (args.size() == 2) {
                throw new RuntimeException("teleport() with location object not yet supported");
            } else {
                double x = args.get(1).asNumber();
                double y = args.get(2).asNumber();
                double z = args.get(3).asNumber();

                player.teleport(x, y, z);
                context.log("[TELEPORT] " + player.getName() + " teleported to (" + x + ", " + y + ", " + z + ")");
            }

            return new BooleanValue(true);
        }
    }

    public static class GiveFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public GiveFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException("give() requires 2 or 3 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("give() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            String itemId = args.get(1).asString();
            int amount = args.size() == 3 ? (int) args.get(2).asNumber() : 1;

            player.giveItem(itemId, amount);
            context.log("[GIVE] " + player.getName() + " received " + amount + "x " + itemId);

            return new BooleanValue(true);
        }
    }

    public static class RemoveItemFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public RemoveItemFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException("remove_item() requires 2 or 3 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("remove_item() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            String itemId = args.get(1).asString();
            int amount = args.size() == 3 ? (int) args.get(2).asNumber() : 1;

            player.removeItem(itemId, amount);
            context.log("[REMOVE_ITEM] " + player.getName() + " lost " + amount + "x " + itemId);

            return new NumberValue(amount);
        }
    }

    public static class SetHealthFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SetHealthFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("set_health() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("set_health() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            double health = args.get(1).asNumber();

            if (health < 0 || health > 20) {
                throw new RuntimeException("set_health() expects health between 0 and 20, got " + health);
            }

            player.setHealth(health);
            context.log("[HEALTH] " + player.getName() + " health set to " + health);

            return new BooleanValue(true);
        }
    }

    public static class SetHungerFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SetHungerFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("set_hunger() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("set_hunger() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            int hunger = (int) args.get(1).asNumber();

            if (hunger < 0 || hunger > 20) {
                throw new RuntimeException("set_hunger() expects hunger between 0 and 20, got " + hunger);
            }

            player.setHunger(hunger);
            context.log("[HUNGER] " + player.getName() + " hunger set to " + hunger);

            return new BooleanValue(true);
        }
    }

    public static class SetGamemodeFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SetGamemodeFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("set_gamemode() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("set_gamemode() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            String gamemode = args.get(1).asString().toLowerCase();

            java.util.Set<String> validModes = java.util.Set.of("adventure", "creative", "spectator", "survival");
            if (!validModes.contains(gamemode)) {
                throw new RuntimeException("set_gamemode() expects one of [adventure, creative, spectator, survival], got " + gamemode);
            }

            player.setGamemode(gamemode);
            context.log("[GAMEMODE] " + player.getName() + " gamemode set to " + gamemode);

            return new BooleanValue(true);
        }
    }

    public static class HealFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public HealFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("heal() requires 1 argument, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("heal() expects player argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            player.heal(player.getMaxHealth());
            context.log("[HEAL] " + player.getName() + " healed to full health");

            return new BooleanValue(true);
        }
    }

    public static class KillFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public KillFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("kill() requires 1 argument, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("kill() expects player argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            player.setHealth(0);
            context.log("[KILL] " + player.getName() + " was killed");

            return new BooleanValue(true);
        }
    }

    public static class SendMessageFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SendMessageFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("sendmessage() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("sendmessage() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            String message = args.get(1).asString();

            context.addMessage(player.getName(), message);

            return new BooleanValue(true);
        }
    }

    public static class GetHealthFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_health() requires 1 argument, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("get_health() expects player argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            return new NumberValue(player.getHealth());
        }
    }

    public static class GetHungerFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_hunger() requires 1 argument, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("get_hunger() expects player argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            return new NumberValue(player.getHunger());
        }
    }

    public static class GetNameFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_name() requires 1 argument, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("get_name() expects player argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            return new StringValue(player.getName());
        }
    }

    public static class GetLocationFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_location() requires 1 argument, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("get_location() expects player argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            MapValue location = new MapValue(new java.util.HashMap<>());
            location.put("x", new NumberValue(player.getX()));
            location.put("y", new NumberValue(player.getY()));
            location.put("z", new NumberValue(player.getZ()));

            return location;
        }
    }

    public static class GetGamemodeFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_gamemode() requires 1 argument, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("get_gamemode() expects player argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            return new StringValue(player.getGamemode());
        }
    }

    public static class SetFlyingFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public SetFlyingFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("set_flying() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("set_flying() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            boolean flying = args.get(1).asBoolean();

            player.setFlying(flying);
            context.log("[FLYING] " + player.getName() + " flying set to " + flying);

            return new BooleanValue(true);
        }
    }

    public static class DamageFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public DamageFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("damage() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isPlayer()) {
                throw new RuntimeException("damage() expects player as first argument, got " + args.get(0).getType());
            }

            MockPlayer player = (MockPlayer) args.get(0).getValue();
            double damage = args.get(1).asNumber();

            player.damage(damage);
            context.log("[DAMAGE] " + player.getName() + " took " + damage + " damage");

            return new BooleanValue(true);
        }
    }
}