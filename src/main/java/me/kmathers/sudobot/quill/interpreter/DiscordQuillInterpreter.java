package me.kmathers.sudobot.quill.interpreter;

import me.kmathers.sudobot.quill.parser.AST;
import me.kmathers.sudobot.quill.parser.AST.*;
import me.kmathers.sudobot.quill.interpreter.QuillValue.*;
import me.kmathers.sudobot.quill.interpreter.ScopeContext.Region;
import me.kmathers.sudobot.quill.simulation.DiscordSimulationContext;

import java.util.*;

/**
 * Discord-compatible Quill interpreter.
 * Adapted from QuillInterpreter to work without Bukkit dependencies.
 * Uses mock objects for Player, World, Location, etc.
 */
public class DiscordQuillInterpreter {
    private ScopeContext globalScope;
    private ScopeContext currentScope;
    private Map<String, BuiltInFunction> builtIns;
    private Map<String, List<EventHandler>> eventHandlers;
    private DiscordSimulationContext simulationContext;
    private ThreadLocal<LoopDetector> loopDetector;

    private static class ReturnSignal extends RuntimeException {
        final QuillValue value;
        ReturnSignal(QuillValue value) { this.value = value; }
    }
    
    private static class BreakSignal extends RuntimeException {}
    private static class ContinueSignal extends RuntimeException {}

    public DiscordQuillInterpreter(DiscordSimulationContext simulationContext) {
        this.simulationContext = simulationContext;
        this.globalScope = new ScopeContext("global", new Region(0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        this.currentScope = globalScope;
        this.builtIns = new HashMap<>();
        this.eventHandlers = new HashMap<>();
        this.loopDetector = ThreadLocal.withInitial(() -> new LoopDetector(10000, 5000));
        registerBuiltIns();
    }
    
    // === Main Evaluation ===
    
    public void execute(Program program) {
        executeStatements(program.statements, 0);
    }

    private void executeStatements(List<ASTNode> statements, int startIndex) {
        for (int i = startIndex; i < statements.size(); i++) {
            try {
                evaluate(statements.get(i));
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().startsWith("QL-INTERNAL-CALL-WAIT:")) {
                    int ticks = Integer.parseInt(e.getMessage().substring("QL-INTERNAL-CALL-WAIT:".length()));
                    simulationContext.log("[WAIT] " + ticks + " ticks");
                    continue;
                }
                throw e;
            }
        }
    }

    public QuillValue evaluate(ASTNode node) {
        if (node == null) {
            return NullValue.INSTANCE;
        }
        
        // Literals
        if (node instanceof NumberLiteral) {
            return evaluateNumberLiteral((NumberLiteral) node);
        } else if (node instanceof StringLiteral) {
            return evaluateStringLiteral((StringLiteral) node);
        } else if (node instanceof BooleanLiteral) {
            return evaluateBooleanLiteral((BooleanLiteral) node);
        } else if (node instanceof NullLiteral) {
            return NullValue.INSTANCE;
        } else if (node instanceof ListLiteral) {
            return evaluateListLiteral((ListLiteral) node);
        } else if (node instanceof MapLiteral) {
            return evaluateMapLiteral((MapLiteral) node);
        }
        
        // Identifiers and member access
        else if (node instanceof Identifier) {
            return evaluateIdentifier((Identifier) node);
        } else if (node instanceof MemberExpression) {
            return evaluateMemberExpression((MemberExpression) node);
        } else if (node instanceof IndexExpression) {
            return evaluateIndexExpression((IndexExpression) node);
        }
        
        // Expressions
        else if (node instanceof BinaryExpression) {
            return evaluateBinaryExpression((BinaryExpression) node);
        } else if (node instanceof UnaryExpression) {
            return evaluateUnaryExpression((UnaryExpression) node);
        } else if (node instanceof AssignmentExpression) {
            return evaluateAssignmentExpression((AssignmentExpression) node);
        } else if (node instanceof CallExpression) {
            return evaluateCallExpression((CallExpression) node);
        }
        
        // Statements
        else if (node instanceof VariableDeclaration) {
            return evaluateVariableDeclaration((VariableDeclaration) node);
        } else if (node instanceof FunctionDeclaration) {
            return evaluateFunctionDeclaration((FunctionDeclaration) node);
        } else if (node instanceof ReturnStatement) {
            return evaluateReturnStatement((ReturnStatement) node);
        } else if (node instanceof IfStatement) {
            return evaluateIfStatement((IfStatement) node);
        } else if (node instanceof WhileStatement) {
            return evaluateWhileStatement((WhileStatement) node);
        } else if (node instanceof ForStatement) {
            return evaluateForStatement((ForStatement) node);
        } else if (node instanceof BreakStatement) {
            throw new BreakSignal();
        } else if (node instanceof ContinueStatement) {
            throw new ContinueSignal();
        } else if (node instanceof TryStatement) {
            return evaluateTryStatement((TryStatement) node);
        } else if (node instanceof EventHandler) {
            return evaluateEventHandler((EventHandler) node);
        } else if (node instanceof ScopeCreation) {
            return evaluateScopeCreation((ScopeCreation) node);
        } else if (node instanceof ExpressionStatement) {
            return evaluate(((ExpressionStatement) node).expression);
        }
        
        throw new RuntimeException("Unknown AST node type: " + node.getClass().getName());
    }
    
    // === Literal Evaluation ===
    
    private QuillValue evaluateNumberLiteral(NumberLiteral node) {
        return new NumberValue(node.value);
    }

    private QuillValue evaluateStringLiteral(StringLiteral node) {
        return new StringValue(node.value);
    }
    
    private QuillValue evaluateBooleanLiteral(BooleanLiteral node) {
        return new BooleanValue(node.value);
    }
    
    private QuillValue evaluateListLiteral(ListLiteral node) {
        List<QuillValue> elements = new ArrayList<>();
        for (ASTNode element : node.elements) {
            elements.add(evaluate(element));
        }
        return new ListValue(elements);
    }
    
    private QuillValue evaluateMapLiteral(MapLiteral node) {
        Map<String, QuillValue> map = new HashMap<>();
        for (AST.MapLiteral.MapEntry entry : node.entries) {
            QuillValue value = evaluate(entry.value);
            map.put(entry.key, value);
        }
        return new MapValue(map);
    }
    
    // === Identifier and Member Access ===
    
    private QuillValue evaluateIdentifier(Identifier node) {
        return currentScope.get(node.name);
    }
    
    private QuillValue evaluateMemberExpression(MemberExpression node) {
        QuillValue object = evaluate(node.object);
        
        if (object.isScope()) {
            ScopeContext scope = object.asScope().getScope();
            return scope.get(node.property);
        }
        
        if (object.isMap()) {
            MapValue mapValue = (MapValue) object;
            return mapValue.get(node.property);
        }

        throw new RuntimeException("Cannot access property '" + node.property + "' on type " + object.getType());
    }
    
    private QuillValue evaluateIndexExpression(IndexExpression node) {
        QuillValue object = evaluate(node.object);
        QuillValue index = evaluate(node.index);
        
        if (object.isMap()) {
            String key = index.asString();
            return object.asMap().getOrDefault(key, NullValue.INSTANCE);
        } else if (object.isList()) {
            int idx = (int) index.asNumber();
            List<QuillValue> list = object.asList();
            if (idx < 0 || idx >= list.size()) {
                throw new RuntimeException("Index " + idx + " out of bounds for list of size " + list.size());
            }
            return list.get(idx);
        }
        
        throw new RuntimeException("Cannot index type " + object.getType());
    }

    // === Binary Expressions ===
    
    private QuillValue evaluateBinaryExpression(BinaryExpression node) {
        QuillValue left = evaluate(node.left);
        QuillValue right = evaluate(node.right);
        
        switch (node.operator) {
            case "+":
                if (left.isNumber() && right.isNumber()) {
                    return new NumberValue(left.asNumber() + right.asNumber());
                }
                return new StringValue(left.toString() + right.toString());
                
            case "-":
                return new NumberValue(left.asNumber() - right.asNumber());
            case "*":
                return new NumberValue(left.asNumber() * right.asNumber());
            case "/":
                if (right.asNumber() == 0) {
                    throw new RuntimeException("Division by zero");
                }
                return new NumberValue(left.asNumber() / right.asNumber());
            case "%":
                return new NumberValue(left.asNumber() % right.asNumber());
                
            case "==":
                return new BooleanValue(isEqual(left, right));
            case "!=":
                return new BooleanValue(!isEqual(left, right));
            case ">":
                return new BooleanValue(left.asNumber() > right.asNumber());
            case "<":
                return new BooleanValue(left.asNumber() < right.asNumber());
            case ">=":
                return new BooleanValue(left.asNumber() >= right.asNumber());
            case "<=":
                return new BooleanValue(left.asNumber() <= right.asNumber());
                
            case "&&":
                return new BooleanValue(left.isTruthy() && right.isTruthy());
            case "||":
                return new BooleanValue(left.isTruthy() || right.isTruthy());
                
            default:
                throw new RuntimeException("Unknown binary operator: " + node.operator);
        }
    }
    
    private boolean isEqual(QuillValue left, QuillValue right) {
        if (left.isNull() && right.isNull()) return true;
        if (left.isNull() || right.isNull()) return false;
        if (left.getType() != right.getType()) return false;
        
        if (left.isNumber()) return left.asNumber() == right.asNumber();
        if (left.isString()) return left.asString().equals(right.asString());
        if (left.isBoolean()) return left.asBoolean() == right.asBoolean();
        
        return left.getValue() == right.getValue();
    }
    
    // === Unary Expressions ===
    
    private QuillValue evaluateUnaryExpression(UnaryExpression node) {
        QuillValue operand = evaluate(node.operand);
        
        switch (node.operator) {
            case "!":
                return new BooleanValue(!operand.isTruthy());
            case "-":
                return new NumberValue(-operand.asNumber());
            default:
                throw new RuntimeException("Unknown unary operator: " + node.operator);
        }
    }
    
    // === Assignment ===
    private QuillValue evaluateAssignmentExpression(AssignmentExpression node) {
        QuillValue value = evaluate(node.value);
        
        if (node.target instanceof Identifier) {
            String name = ((Identifier) node.target).name;
            currentScope.set(name, value);
            return value;
        } else if (node.target instanceof MemberExpression) {
            MemberExpression member = (MemberExpression) node.target;
            QuillValue object = evaluate(member.object);
            
            if (object.isScope()) {
                ScopeContext scope = object.asScope().getScope();
                scope.set(member.property, value);
                return value;
            }
            
            if (object.isMap()) {
                MapValue mapValue = (MapValue) object;
                mapValue.put(member.property, value);
                return value;
            }
            
            throw new RuntimeException("Cannot assign to member of type " + object.getType());
        } else if (node.target instanceof IndexExpression) {
            IndexExpression indexExpr = (IndexExpression) node.target;
            QuillValue object = evaluate(indexExpr.object);
            QuillValue index = evaluate(indexExpr.index);
            
            if (object.isMap()) {
                String key = index.asString();
                MapValue mapValue = (MapValue) object;
                mapValue.put(key, value);
                return value;
            } else if (object.isList()) {
                int idx = (int) index.asNumber();
                List<QuillValue> list = object.asList();
                if (idx < 0 || idx >= list.size()) {
                    throw new RuntimeException("Index " + idx + " out of bounds");
                }
                list.set(idx, value);
                return value;
            }
            
            throw new RuntimeException("Cannot index type " + object.getType());
        }
                
        throw new RuntimeException("Invalid assignment target");
    }
    
    // === Function Calls ===
    
    private QuillValue evaluateCallExpression(CallExpression node) {
        if (node.callee instanceof Identifier) {
            String name = ((Identifier) node.callee).name;
            if (builtIns.containsKey(name)) {
                List<QuillValue> args = new ArrayList<>();
                for (ASTNode arg : node.arguments) {
                    args.add(evaluate(arg));
                }
                return builtIns.get(name).call(args, currentScope, this);
            }
        }
        
        QuillValue callee = evaluate(node.callee);
        List<QuillValue> args = new ArrayList<>();
        for (ASTNode arg : node.arguments) {
            args.add(evaluate(arg));
        }
        
        if (callee.isFunction()) {
            FunctionValue func = (FunctionValue) callee;
            
            ScopeContext funcScope = new ScopeContext("function_" + func.getName(), currentScope);
            
            if (args.size() != func.getParameters().size()) {
                throw new RuntimeException("Function " + func.getName() + " expects " + 
                    func.getParameters().size() + " arguments, got " + args.size());
            }
            
            for (int i = 0; i < args.size(); i++) {
                funcScope.define(func.getParameters().get(i), args.get(i));
            }
            
            ScopeContext previousScope = currentScope;
            currentScope = funcScope;
            
            try {
                if (func.getBody() instanceof FunctionDeclaration) {
                    FunctionDeclaration funcDecl = (FunctionDeclaration) func.getBody();
                    for (ASTNode statement : funcDecl.body) {
                        evaluate(statement);
                    }
                }
                return NullValue.INSTANCE;
            } catch (ReturnSignal ret) {
                return ret.value;
            } finally {
                currentScope = previousScope;
            }
        }
        
        throw new RuntimeException("Cannot call type " + callee.getType());
    }
    
    // === Statements ===
    
    private QuillValue evaluateVariableDeclaration(VariableDeclaration node) {
        QuillValue value = evaluate(node.value);

        if (node.isConst) {
            currentScope.defineConst(node.name, value);
        } else {
            currentScope.define(node.name, value);
        }
        
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateFunctionDeclaration(FunctionDeclaration node) {
        FunctionValue func = new FunctionValue(
            node.name,
            node.parameters,
            node,
            currentScope
        );
        currentScope.define(node.name, func);
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateReturnStatement(ReturnStatement node) {
        QuillValue value = node.value != null ? evaluate(node.value) : NullValue.INSTANCE;
        throw new ReturnSignal(value);
    }
    
    private QuillValue evaluateIfStatement(IfStatement node) {
        QuillValue condition = evaluate(node.condition);
        
        if (condition.isTruthy()) {
            for (ASTNode statement : node.thenBranch) {
                evaluate(statement);
            }
        } else if (node.elseBranch != null) {
            for (ASTNode statement : node.elseBranch) {
                evaluate(statement);
            }
        }
        
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateWhileStatement(WhileStatement node) {
        LoopDetector detector = loopDetector.get();
        detector.startLoop();

        try {
            while (evaluate(node.condition).isTruthy()) {
                detector.checkIteration();
                try {
                    for (ASTNode statement : node.body) {
                        evaluate(statement);
                    }
                } catch (ContinueSignal c) {
                    continue;
                }
            }
        } catch (BreakSignal b) {
            // Break out of loop
        } catch (LoopDetector.InfiniteLoopException e) {
            throw new RuntimeException("Infinite loop detected: " + e.getMessage());
        } finally {
            detector.endLoop();
        }
        
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateForStatement(ForStatement node) {
        QuillValue iterable = evaluate(node.iterable);
        LoopDetector detector = loopDetector.get();

        if (!iterable.isList()) {
            throw new RuntimeException("For loop expects a list, got " + iterable.getType());
        }
        
        List<QuillValue> items = iterable.asList();
        detector.startLoop();

        try {
            for (QuillValue item : items) {
                detector.checkIteration();
                ScopeContext iterationScope = new ScopeContext("for_iteration", currentScope);
                
                ScopeContext previousScope = currentScope;
                currentScope = iterationScope;
                
                try {
                    currentScope.define(node.variable, item);
                    
                    try {
                        for (ASTNode statement : node.body) {
                            evaluate(statement);
                        }
                    } catch (ContinueSignal c) {
                        continue;
                    }
                } finally {
                    currentScope = previousScope;
                }
            }
        } catch (BreakSignal b) {
            // Break out of loop
        } catch (LoopDetector.InfiniteLoopException e) {
            throw new RuntimeException("Infinite loop detected: " + e.getMessage());
        } finally {
            detector.endLoop();
        }
        
        return NullValue.INSTANCE;
    }

    private QuillValue evaluateTryStatement(TryStatement node) {
        try {
            for (ASTNode statement : node.tryBlock) {
                evaluate(statement);
            }
        } catch (Exception e) {
            ScopeContext catchScope = new ScopeContext("try_catch", currentScope);
            catchScope.define(node.errorVariable, new StringValue(e.getMessage()));
            
            ScopeContext previousScope = currentScope;
            currentScope = catchScope;
            
            try {
                for (ASTNode statement : node.catchBlock) {
                    evaluate(statement);
                }
            } finally {
                currentScope = previousScope;
            }
        }
        
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateEventHandler(EventHandler node) {
        eventHandlers.computeIfAbsent(node.eventName, k -> new ArrayList<>()).add(node);
        simulationContext.log("[EVENT] Registered handler for event: " + node.eventName);
        return NullValue.INSTANCE;
    }
    
    private QuillValue evaluateScopeCreation(ScopeCreation node) {
        if (node.arguments.size() != 6) {
            throw new RuntimeException("Scope creation requires 6 boundary arguments (x1, y1, z1, x2, y2, z2)");
        }
        
        double x1 = evaluate(node.arguments.get(0)).asNumber();
        double y1 = evaluate(node.arguments.get(1)).asNumber();
        double z1 = evaluate(node.arguments.get(2)).asNumber();
        double x2 = evaluate(node.arguments.get(3)).asNumber();
        double y2 = evaluate(node.arguments.get(4)).asNumber();
        double z2 = evaluate(node.arguments.get(5)).asNumber();
        
        ScopeContext.Region region = new ScopeContext.Region(x1, y1, z1, x2, y2, z2);
        ScopeContext newScope = new ScopeContext("subscope", currentScope, region);
        
        return new ScopeValue(newScope);
    }
    
    // === Event Handling ===
    
    public void triggerEvent(String eventName, Map<String, QuillValue> eventContext) {
        List<EventHandler> handlers = eventHandlers.get(eventName);
        if (handlers == null || handlers.isEmpty()) return;
        
        simulationContext.log("[TRIGGER] Event: " + eventName);
        
        for (EventHandler handler : handlers) {
            ScopeContext eventScope = new ScopeContext("event_" + eventName, globalScope);
            for (Map.Entry<String, QuillValue> entry : eventContext.entrySet()) {
                eventScope.define(entry.getKey(), entry.getValue());
            }
            
            ScopeContext previousScope = currentScope;
            currentScope = eventScope;
            
            try {
                for (ASTNode statement : handler.body) {
                    evaluate(statement);
                }
            } catch (Exception e) {
                simulationContext.log("[ERROR] Error in event handler " + eventName + ": " + e.getMessage());
            } finally {
                currentScope = previousScope;
            }
        }
    }
    
    // === Built-in Functions Registration ===
    
    private void registerBuiltIns() {
        // === Utility Functions ===
        builtIns.put("log", new DiscordBuiltInUtilFuncs.LogFunction(simulationContext));
        builtIns.put("len", new DiscordBuiltInUtilFuncs.LenFunction());
        builtIns.put("append", new DiscordBuiltInUtilFuncs.AppendFunction());
        builtIns.put("remove", new DiscordBuiltInUtilFuncs.RemoveFunction());
        builtIns.put("contains", new DiscordBuiltInUtilFuncs.ContainsFunction());
        builtIns.put("split", new DiscordBuiltInUtilFuncs.SplitFunction());
        builtIns.put("join", new DiscordBuiltInUtilFuncs.JoinFunction());
        builtIns.put("to_string", new DiscordBuiltInUtilFuncs.ToStringFunction());
        builtIns.put("to_number", new DiscordBuiltInUtilFuncs.ToNumberFunction());
        builtIns.put("to_boolean", new DiscordBuiltInUtilFuncs.ToBooleanFunction());
        builtIns.put("type_of", new DiscordBuiltInUtilFuncs.TypeOfFunction());
        builtIns.put("range", new DiscordBuiltInUtilFuncs.RangeFunction());
        builtIns.put("random", new DiscordBuiltInUtilFuncs.RandomFunction());
        builtIns.put("round", new DiscordBuiltInUtilFuncs.RoundFunction());
        builtIns.put("floor", new DiscordBuiltInUtilFuncs.FloorFunction());
        builtIns.put("ceil", new DiscordBuiltInUtilFuncs.CeilFunction());
        builtIns.put("abs", new DiscordBuiltInUtilFuncs.AbsFunction());
        builtIns.put("sqrt", new DiscordBuiltInUtilFuncs.SqrtFunction());
        builtIns.put("pow", new DiscordBuiltInUtilFuncs.PowFunction());
        builtIns.put("random_choice", new DiscordBuiltInUtilFuncs.RandomChoiceFunction());
        builtIns.put("min", new DiscordBuiltInUtilFuncs.MinFunction());
        builtIns.put("max", new DiscordBuiltInUtilFuncs.MaxFunction());
        builtIns.put("sum", new DiscordBuiltInUtilFuncs.SumFunction());
        builtIns.put("avg", new DiscordBuiltInUtilFuncs.AvgFunction());
        builtIns.put("get_player", new DiscordBuiltInUtilFuncs.GetPlayerFunction(simulationContext));
        builtIns.put("get_online_players", new DiscordBuiltInUtilFuncs.GetOnlinePlayersFunction(simulationContext));

        // === Player Functions ===
        builtIns.put("teleport", new DiscordBuiltInPlayerFuncs.TeleportFunction(simulationContext));
        builtIns.put("give", new DiscordBuiltInPlayerFuncs.GiveFunction(simulationContext));
        builtIns.put("remove_item", new DiscordBuiltInPlayerFuncs.RemoveItemFunction(simulationContext));
        builtIns.put("set_health", new DiscordBuiltInPlayerFuncs.SetHealthFunction(simulationContext));
        builtIns.put("set_hunger", new DiscordBuiltInPlayerFuncs.SetHungerFunction(simulationContext));
        builtIns.put("set_gamemode", new DiscordBuiltInPlayerFuncs.SetGamemodeFunction(simulationContext));
        builtIns.put("heal", new DiscordBuiltInPlayerFuncs.HealFunction(simulationContext));
        builtIns.put("kill", new DiscordBuiltInPlayerFuncs.KillFunction(simulationContext));
        builtIns.put("sendmessage", new DiscordBuiltInPlayerFuncs.SendMessageFunction(simulationContext));
        builtIns.put("get_health", new DiscordBuiltInPlayerFuncs.GetHealthFunction());
        builtIns.put("get_hunger", new DiscordBuiltInPlayerFuncs.GetHungerFunction());
        builtIns.put("get_name", new DiscordBuiltInPlayerFuncs.GetNameFunction());
        builtIns.put("get_location", new DiscordBuiltInPlayerFuncs.GetLocationFunction());
        builtIns.put("get_gamemode", new DiscordBuiltInPlayerFuncs.GetGamemodeFunction());
        builtIns.put("set_flying", new DiscordBuiltInPlayerFuncs.SetFlyingFunction(simulationContext));
        builtIns.put("damage", new DiscordBuiltInPlayerFuncs.DamageFunction(simulationContext));
        
        // === World Functions ===
        builtIns.put("set_block", new DiscordBuiltInWorldFuncs.SetBlockFunction(simulationContext));
        builtIns.put("get_block", new DiscordBuiltInWorldFuncs.GetBlockFunction(simulationContext));
        builtIns.put("break_block", new DiscordBuiltInWorldFuncs.BreakBlockFunction(simulationContext));
        builtIns.put("set_time", new DiscordBuiltInWorldFuncs.SetTimeFunction(simulationContext));
        builtIns.put("get_time", new DiscordBuiltInWorldFuncs.GetTimeFunction(simulationContext));
        builtIns.put("set_weather", new DiscordBuiltInWorldFuncs.SetWeatherFunction(simulationContext));
        builtIns.put("get_weather", new DiscordBuiltInWorldFuncs.GetWeatherFunction(simulationContext));
        builtIns.put("broadcast", new DiscordBuiltInWorldFuncs.BroadcastFunction(simulationContext));
        builtIns.put("create_world", new DiscordBuiltInWorldFuncs.CreateWorldFunction(simulationContext));
        builtIns.put("get_world", new DiscordBuiltInWorldFuncs.GetWorldFunction(simulationContext));
        builtIns.put("spawn_entity", new DiscordBuiltInWorldFuncs.SpawnEntityFunction(simulationContext));
        builtIns.put("distance", new DiscordBuiltInWorldFuncs.DistanceFunction());
    }
    
    public interface BuiltInFunction {
        QuillValue call(List<QuillValue> args, ScopeContext scope, DiscordQuillInterpreter interpreter);
    }

    public String getScopeName() {
        return globalScope.getName();
    }

    public ScopeContext getGlobalScope() {
        return globalScope;
    }

    public Set<String> getRegisteredEvents() {
        return eventHandlers.keySet();
    }
}