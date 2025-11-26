package me.kmathers.sudobot.quill.interpreter;

import me.kmathers.sudobot.quill.simulation.DiscordSimulationContext;
import me.kmathers.sudobot.quill.interpreter.QuillValue.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Discord-compatible built-in utility functions.
 * Provides core utility operations for Quill scripts.
 */
public class DiscordBuiltInUtilFuncs {
    
    public static class LogFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public LogFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("log() requires 1 argument, got " + args.size());
            }
            
            context.log("[LOG] " + args.get(0).toString());
            return new BooleanValue(true);
        }
    }

    public static class LenFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("len() requires 1 argument, got " + args.size());
            }

            QuillValue val = args.get(0);
            if (val.isList()) {
                return new NumberValue(val.asList().size());
            } else if (val.isString()) {
                return new NumberValue(val.asString().length());
            } else if (val.isMap()) {
                return new NumberValue(val.asMap().size());
            } else {
                throw new RuntimeException("len() expects list, string, or map, got " + val.getType());
            }
        }
    }

    public static class AppendFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("append() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isList()) {
                throw new RuntimeException("append() expects list as first argument, got " + args.get(0).getType());
            }

            args.get(0).asList().add(args.get(1));
            return new BooleanValue(true);
        }
    }

    public static class RemoveFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("remove() requires 2 arguments, got " + args.size());
            }

            if (!args.get(0).isList()) {
                throw new RuntimeException("remove() expects list as first argument, got " + args.get(0).getType());
            }

            if (!args.get(1).isNumber()) {
                throw new RuntimeException("remove() expects number as second argument, got " + args.get(1).getType());
            }

            List<QuillValue> list = args.get(0).asList();
            int index = (int) args.get(1).asNumber();

            if (index < 0 || index >= list.size()) {
                throw new RuntimeException("Index " + index + " out of bounds for list of size " + list.size());
            }

            list.remove(index);
            return new BooleanValue(true);
        }
    }

    public static class ContainsFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("contains() requires 2 arguments, got " + args.size());
            }

            QuillValue container = args.get(0);
            QuillValue searchItem = args.get(1);

            if (container.isList()) {
                for (QuillValue item : container.asList()) {
                    if (valuesEqual(item, searchItem)) {
                        return new BooleanValue(true);
                    }
                }
                return new BooleanValue(false);
            } else if (container.isString()) {
                return new BooleanValue(container.asString().contains(searchItem.asString()));
            } else {
                throw new RuntimeException("contains() expects list or string, got " + container.getType());
            }
        }

        private boolean valuesEqual(QuillValue a, QuillValue b) {
            if (a.getType() != b.getType()) return false;
            if (a.isNumber()) return a.asNumber() == b.asNumber();
            if (a.isString()) return a.asString().equals(b.asString());
            if (a.isBoolean()) return a.asBoolean() == b.asBoolean();
            if (a.isNull()) return b.isNull();
            return a.getValue() == b.getValue();
        }
    }

    public static class SplitFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("split() requires 2 arguments, got " + args.size());
            }

            String str = args.get(0).asString();
            String delimiter = args.get(1).asString();

            return new ListValue(
                Arrays.stream(str.split(java.util.regex.Pattern.quote(delimiter)))
                    .map(StringValue::new)
                    .collect(Collectors.toList())
            );
        }
    }

    public static class JoinFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("join() requires 2 arguments, got " + args.size());
            }

            List<QuillValue> list = args.get(0).asList();
            String delimiter = args.get(1).asString();

            return new StringValue(
                list.stream()
                    .map(v -> v.toString())
                    .collect(Collectors.joining(delimiter))
            );
        }
    }

    public static class ToStringFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("to_string() requires 1 argument, got " + args.size());
            }

            return new StringValue(args.get(0).toString());
        }
    }

    public static class ToNumberFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("to_number() requires 1 argument, got " + args.size());
            }

            QuillValue val = args.get(0);

            if (val.isNumber()) {
                return val;
            } else if (val.isString()) {
                try {
                    return new NumberValue(Double.parseDouble(val.asString()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Cannot convert string '" + val.asString() + "' to number");
                }
            } else if (val.isBoolean()) {
                return new NumberValue(val.asBoolean() ? 1.0 : 0.0);
            } else {
                throw new RuntimeException("Cannot convert " + val.getType() + " to number");
            }
        }
    }

    public static class ToBooleanFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("to_boolean() requires 1 argument, got " + args.size());
            }

            return new BooleanValue(args.get(0).isTruthy());
        }
    }

    public static class TypeOfFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("type_of() requires 1 argument, got " + args.size());
            }

            return new StringValue(args.get(0).getType().toString().toLowerCase());
        }
    }

    public static class RangeFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("range() requires 2 arguments, got " + args.size());
            }

            int start = (int) args.get(0).asNumber();
            int end = (int) args.get(1).asNumber();

            List<QuillValue> values = new ArrayList<>();
            if (start <= end) {
                for (int i = start; i < end; i++) {
                    values.add(new NumberValue(i));
                }
            } else {
                for (int i = start; i > end; i--) {
                    values.add(new NumberValue(i));
                }
            }

            return new ListValue(values);
        }
    }

    public static class RandomFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private static Random random = new Random();

        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() < 1 || args.size() > 2) {
                throw new RuntimeException("random() requires 1 or 2 arguments, got " + args.size());
            }

            double min = 0;
            double max = 0;

            if (args.size() == 1) {
                max = args.get(0).asNumber();
            } else {
                min = args.get(0).asNumber();
                max = args.get(1).asNumber();
            }

            double choice = random.nextDouble() * (max - min) + min;
            return new NumberValue(choice);
        }
    }

    public static class RoundFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("round() requires 1 argument, got " + args.size());
            }

            return new NumberValue(Math.round(args.get(0).asNumber()));
        }
    }

    public static class FloorFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("floor() requires 1 argument, got " + args.size());
            }

            return new NumberValue(Math.floor(args.get(0).asNumber()));
        }
    }

    public static class CeilFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("ceil() requires 1 argument, got " + args.size());
            }

            return new NumberValue(Math.ceil(args.get(0).asNumber()));
        }
    }

    public static class AbsFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("abs() requires 1 argument, got " + args.size());
            }

            return new NumberValue(Math.abs(args.get(0).asNumber()));
        }
    }

    public static class SqrtFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("sqrt() requires 1 argument, got " + args.size());
            }

            return new NumberValue(Math.sqrt(args.get(0).asNumber()));
        }
    }

    public static class PowFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException("pow() requires 2 arguments, got " + args.size());
            }

            return new NumberValue(Math.pow(args.get(0).asNumber(), args.get(1).asNumber()));
        }
    }

    public static class RandomChoiceFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private static Random random = new Random();

        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("random_choice() requires 1 argument, got " + args.size());
            }

            List<QuillValue> list = args.get(0).asList();

            if (list.isEmpty()) {
                throw new RuntimeException("random_choice() requires non-empty list");
            }

            int index = random.nextInt(list.size());
            return list.get(index);
        }
    }

    public static class MinFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() < 1) {
                throw new RuntimeException("min() requires at least 1 argument");
            }

            if (args.size() == 1 && args.get(0).isList()) {
                List<QuillValue> list = args.get(0).asList();
                if (list.isEmpty()) {
                    throw new RuntimeException("min() requires non-empty list");
                }

                double min = Double.POSITIVE_INFINITY;
                for (QuillValue val : list) {
                    min = Math.min(min, val.asNumber());
                }
                return new NumberValue(min);
            }

            double min = Double.POSITIVE_INFINITY;
            for (QuillValue val : args) {
                min = Math.min(min, val.asNumber());
            }
            return new NumberValue(min);
        }
    }

    public static class MaxFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() < 1) {
                throw new RuntimeException("max() requires at least 1 argument");
            }

            if (args.size() == 1 && args.get(0).isList()) {
                List<QuillValue> list = args.get(0).asList();
                if (list.isEmpty()) {
                    throw new RuntimeException("max() requires non-empty list");
                }

                double max = Double.NEGATIVE_INFINITY;
                for (QuillValue val : list) {
                    max = Math.max(max, val.asNumber());
                }
                return new NumberValue(max);
            }

            double max = Double.NEGATIVE_INFINITY;
            for (QuillValue val : args) {
                max = Math.max(max, val.asNumber());
            }
            return new NumberValue(max);
        }
    }

    public static class SumFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("sum() requires 1 argument, got " + args.size());
            }

            if (!args.get(0).isList()) {
                throw new RuntimeException("sum() expects list, got " + args.get(0).getType());
            }

            double sum = 0;
            for (QuillValue val : args.get(0).asList()) {
                sum += val.asNumber();
            }
            return new NumberValue(sum);
        }
    }

    public static class AvgFunction implements DiscordQuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("avg() requires 1 argument, got " + args.size());
            }

            List<QuillValue> list = args.get(0).asList();
            if (list.isEmpty()) {
                throw new RuntimeException("avg() requires non-empty list");
            }

            double sum = 0;
            for (QuillValue val : list) {
                sum += val.asNumber();
            }
            return new NumberValue(sum / list.size());
        }
    }

    public static class GetPlayerFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public GetPlayerFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException("get_player() requires 1 argument, got " + args.size());
            }
            
            String playerName = args.get(0).asString();
            
            if (!context.hasPlayer(playerName)) {
                throw new RuntimeException("Player '" + playerName + "' not found");
            }
            
            DiscordSimulationContext.MockPlayer player = context.getPlayer(playerName);
            return new PlayerValue(player);
        }
    }

    public static class GetOnlinePlayersFunction implements DiscordQuillInterpreter.BuiltInFunction {
        private final DiscordSimulationContext context;
        
        public GetOnlinePlayersFunction(DiscordSimulationContext context) {
            this.context = context;
        }
        
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter) {
            if (args.size() != 0) {
                throw new RuntimeException("get_online_players() requires 0 arguments, got " + args.size());
            }
            
            List<QuillValue> players = new ArrayList<>();
            for (DiscordSimulationContext.MockPlayer player : context.getAllPlayers()) {
                players.add(new PlayerValue(player));
            }
            
            return new ListValue(players);
        }
    }
}