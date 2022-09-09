package lang.toyscript.engine.runtime.visitor;

import lang.toyscript.engine.error.UncheckedScriptException;
import lang.toyscript.engine.runtime.registry.Registry;
import lang.toyscript.engine.runtime.stack.PanicChannel;
import lang.toyscript.engine.runtime.stack.VarStack;
import lang.toyscript.parser.ToyScriptLexer;
import lang.toyscript.parser.ToyScriptParser;
import lang.toyscript.parser.ToyScriptVisitor;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static lang.toyscript.engine.runtime.type.TypeUtils.boolCast;
import static lang.toyscript.engine.runtime.type.TypeUtils.ensureType;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberAdd;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberCast;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberDiv;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberEquals;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberGreaterThen;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberLessThen;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberMod;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberMul;
import static lang.toyscript.engine.runtime.type.TypeUtils.numberSub;
import static lang.toyscript.engine.runtime.type.TypeUtils.unaryMin;

public class ParseTreeVisitor extends AbstractParseTreeVisitor<Void> implements ToyScriptVisitor<Void> {

    static final Logger LOGGER = LoggerFactory.getLogger(ParseTreeVisitor.class);

    public static ParseTreeVisitor create(ScriptContext scriptContext) {
        var registry = Registry.scoped(scriptContext);
        var stack = VarStack.create();
        var panicChannel = PanicChannel.create();
        return LOGGER.isDebugEnabled() ?
                new DebugParseTreeVisitor(registry, stack, panicChannel) :
                new ParseTreeVisitor(registry, stack, panicChannel);
    }

    private final VarStack stack;

    private final Registry registry;

    private final PanicChannel panicToken;

    private Object result;

    ParseTreeVisitor(Registry registry, VarStack stack, PanicChannel panicChannel) {
        this.stack = stack;
        this.registry = registry;
        this.panicToken = panicChannel;
    }

    @Override
    public Void visitProgram(ToyScriptParser.ProgramContext ctx) {
        for (var statement : ctx.statement()) {
            visit(statement);
            if (panicToken.isPresent()) {
                var token = panicToken.pop();
                var type = token.getType();
                if (type == ToyScriptLexer.EXIT) {
                    result = stack.pop();
                    break;
                }
                if (type == ToyScriptLexer.THROW) {
                    var err = stack.pop();
                    throw new UncheckedScriptException("Uncaught error: " + err, token);
                }
                throw new UncheckedScriptException("Token <" + token.getText() + "> in illegal position", token);
            }
        }
        return null;
    }

    @Override
    public Void visitReturnExitClause(ToyScriptParser.ReturnExitClauseContext ctx) {
        if (ctx.expr() == null) stack.push(null);
        else visit(ctx.expr());
        panicToken.push(ctx.op);
        return null;
    }

    @Override
    public Void visitStatement(ToyScriptParser.StatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitArrayDefExpr(ToyScriptParser.ArrayDefExprContext ctx) {
        visit(ctx.expr());
        var size = numberCast(stack.pop()).intValue();
        var value = new ArrayList<>(Arrays.asList(new Object[size]));
        stack.push(value);
        return null;
    }

    @Override
    public Void visitIndexAccessExpr(ToyScriptParser.IndexAccessExprContext ctx) {
        var arrExpr = ctx.expr(0);
        var indExpr = ctx.expr(1);
        visit(arrExpr);
        var arr = ensureType(stack.pop(), List.class, arrExpr.start);
        visit(indExpr);
        var index = numberCast(stack.pop()).intValue();
        if (index < 0 || index >= arr.size()) {
            throw new UncheckedScriptException("Index: " + index + " is out of bounds", indExpr.start);
        }
        var value = arr.get(index);
        stack.push(value);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void visitIndexAssignExpr(ToyScriptParser.IndexAssignExprContext ctx) {
        var arrExpr = ctx.expr(0);
        var indExpr = ctx.expr(1);
        var valExpr = ctx.expr(2);
        visit(arrExpr);
        var arr = ensureType(stack.pop(), List.class, arrExpr.start);
        visit(indExpr);
        var index = numberCast(stack.pop()).intValue();
        if (index < 0 || index >= arr.size()) {
            throw new UncheckedScriptException("Index: " + index + " is out of bounds", indExpr.start);
        }
        visit(valExpr);
        var value = stack.pop();
        arr.set(index, value);
        stack.push(value);
        return null;
    }

    @Override
    public Void visitMemberAccessExpr(ToyScriptParser.MemberAccessExprContext ctx) {
        var mapExpr = ctx.expr();
        visit(mapExpr);
        var map = ensureType(stack.pop(), Map.class, mapExpr.start);
        var id = ctx.ID();
        var key = id.getText();
        if (!map.containsKey(key)) {
            throw new UncheckedScriptException("Member: " + key + " not found", id.getSymbol());
        }
        var value = map.get(key);
        stack.push(value);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void visitMemberAssignExpr(ToyScriptParser.MemberAssignExprContext ctx) {
        var mapExpr = ctx.expr(0);
        var valExpr = ctx.expr(1);
        visit(mapExpr);
        var map = ensureType(stack.pop(), Map.class, mapExpr.start);
        var id = ctx.ID();
        var key = id.getText();
        if (!map.containsKey(key)) {
            throw new UncheckedScriptException("Member: " + key + " not found", id.getSymbol());
        }
        visit(valExpr);
        var value = stack.pop();
        map.put(key, value);
        stack.push(value);
        return null;
    }

    @Override
    public Void visitArrayInitExpr(ToyScriptParser.ArrayInitExprContext ctx) {
        var size = ctx.expr().size();
        var value = new ArrayList<>(size);
        for (var expr : ctx.expr()) {
            visit(expr);
            value.add(stack.pop());
        }
        stack.push(value);
        return null;
    }

    @Override
    public Void visitStructInitExpr(ToyScriptParser.StructInitExprContext ctx) {
        var value = new LinkedHashMap<String, Object>();
        for (var i = 0; i < ctx.expr().size(); i++) {
            visit(ctx.expr(i));
            value.put(ctx.ID(i).getText(), stack.pop());
        }
        stack.push(value);
        return null;
    }

    @Override
    public Void visitVarDecl(ToyScriptParser.VarDeclContext ctx) {
        var identifier = ctx.ID();
        var expr = ctx.expr();
        if (expr == null) {
            registry.declare(identifier);
        } else {
            visit(expr);
            var value = stack.pop();
            registry.declare(identifier, value);
        }
        return null;
    }

    @Override
    public Void visitBlockStatement(ToyScriptParser.BlockStatementContext ctx) {
        registry.enterScope();
        for (var statement : ctx.statement()) {
            visit(statement);
            if (panicToken.isPresent()) break;
        }
        registry.exitScope();
        return null;
    }

    @Override
    public Void visitIfStatement(ToyScriptParser.IfStatementContext ctx) {
        visit(ctx.expr());
        var value = stack.pop();
        var condition = boolCast(value);
        var ifStatement = ctx.statement(0);
        var elseStatement = ctx.statement(1);
        if (condition) {
            visit(ifStatement);
        } else if (elseStatement != null) {
            visit(elseStatement);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(ToyScriptParser.WhileStatementContext ctx) {
        visit(ctx.expr());
        var value = stack.pop();
        var condition = boolCast(value);
        while (condition) {
            visit(ctx.statement());
            if (panicToken.isPresent()) {
                if (panicToken.top().getType() == ToyScriptLexer.CONTINUE) panicToken.pop();
                else {
                    if (panicToken.top().getType() == ToyScriptLexer.BREAK) panicToken.pop();
                    break;
                }
            }
            visit(ctx.expr());
            value = stack.pop();
            condition = boolCast(value);
        }
        return null;
    }

    @Override
    public Void visitLoopExitClause(ToyScriptParser.LoopExitClauseContext ctx) {
        panicToken.push(ctx.op);
        return null;
    }

    @Override
    public Void visitExprStatement(ToyScriptParser.ExprStatementContext ctx) {
        ctx.END();
        visit(ctx.expr());
        stack.pop(); // clear stack after standalone expression
        return null;
    }

    @Override
    public Void visitStringLiteralExpr(ToyScriptParser.StringLiteralExprContext ctx) {
        var value = ctx.STRING().getText();
        stack.push(value.substring(1, value.length() - 1));
        return null;
    }

    @Override
    public Void visitAndOrExpr(ToyScriptParser.AndOrExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var value0 = stack.pop();
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.AND -> (boolCast(value0) && boolCast(value1));
            case ToyScriptLexer.OR -> (boolCast(value0) || boolCast(value1));
            default -> throw new IllegalStateException("Unexpected OP token: " + ctx.op.getText());
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitAssignExpr(ToyScriptParser.AssignExprContext ctx) {
        var identifier = ctx.ID();
        var scope = registry.getDeclaringScope(identifier);
        visit(ctx.expr());
        var value = stack.pop();
        registry.assign(identifier, value, scope);
        stack.push(value);
        return null;
    }

    @Override
    public Void visitIncrDecrExpr(ToyScriptParser.IncrDecrExprContext ctx) {
        var identifier = ctx.ID();
        var scope = registry.getDeclaringScope(identifier);
        var value = registry.read(identifier, scope);
        stack.push(value);
        var seed = switch (ctx.op.getType()) {
            case ToyScriptLexer.INCR -> 1;
            case ToyScriptLexer.DECR -> -1;
            default -> throw new UncheckedScriptException("Unexpected token: " + ctx.op.getText(), ctx.op);
        };
        registry.assign(identifier, numberCast(value).intValue() + seed, scope);
        return null;
    }

    @Override
    public Void visitNullLiteralExpr(ToyScriptParser.NullLiteralExprContext ctx) {
        stack.push(null);
        return null;
    }

    @Override
    public Void visitIntLiteralExpr(ToyScriptParser.IntLiteralExprContext ctx) {
        var value = Integer.valueOf(ctx.INT().getText());
        stack.push(value);
        return null;
    }

    @Override
    public Void visitMulDivModExpr(ToyScriptParser.MulDivModExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var value0 = stack.pop();
        var num0 = numberCast(value0);
        var num1 = numberCast(value1);
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.MUL -> numberMul(num0, num1);
            case ToyScriptLexer.DIV -> numberDiv(num0, num1);
            case ToyScriptLexer.MOD -> numberMod(num0, num1);
            default -> throw new UncheckedScriptException("Unexpected token: " + ctx.op.getText(), ctx.op);
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitCompareExpr(ToyScriptParser.CompareExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var value0 = stack.pop();
        var num0 = numberCast(value0);
        var num1 = numberCast(value1);
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.LT -> numberLessThen(num0, num1);
            case ToyScriptLexer.GT -> numberGreaterThen(num0, num1);
            case ToyScriptLexer.LTE -> !numberGreaterThen(num0, num1);
            case ToyScriptLexer.GTE -> !numberLessThen(num0, num1);
            default -> throw new UncheckedScriptException("Unexpected token: " + ctx.op.getText(), ctx.op);
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitBooleanLiteralExpr(ToyScriptParser.BooleanLiteralExprContext ctx) {
        var value = Boolean.valueOf(ctx.BOOL().getText());
        stack.push(value);
        return null;
    }

    @Override
    public Void visitFloatLiteralExpr(ToyScriptParser.FloatLiteralExprContext ctx) {
        var value = Float.valueOf(ctx.FLOAT().getText());
        stack.push(value);
        return null;
    }

    @Override
    public Void visitEqualCheckExpr(ToyScriptParser.EqualCheckExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var value0 = stack.pop();
        var num0 = numberCast(value0);
        var num1 = numberCast(value1);
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.EQ -> numberEquals(num0, num1);
            case ToyScriptLexer.NEQ -> !numberEquals(num0, num1);
            default -> throw new UncheckedScriptException("Unexpected token: " + ctx.op.getText(), ctx.op);
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitVarExpr(ToyScriptParser.VarExprContext ctx) {
        var identifier = ctx.ID();
        var value = registry.read(identifier);
        stack.push(value);
        return null;
    }

    @Override
    public Void visitNestedExpr(ToyScriptParser.NestedExprContext ctx) {
        visit(ctx.expr());
        return null;
    }

    @Override
    public Void visitAddSubExpr(ToyScriptParser.AddSubExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var value0 = stack.pop();
        var num0 = numberCast(value0);
        var num1 = numberCast(value1);
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.PLUS -> numberAdd(num0, num1);
            case ToyScriptLexer.MINUS -> numberSub(num0, num1);
            default -> throw new UncheckedScriptException("Unexpected token: " + ctx.op.getText(), ctx.op);
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitUnaryMinusExpr(ToyScriptParser.UnaryMinusExprContext ctx) {
        visit(ctx.expr());
        var value = stack.pop();
        var result = unaryMin(numberCast(value));
        stack.push(result);
        return null;
    }

    @Override
    public Void visitNegationExpr(ToyScriptParser.NegationExprContext ctx) {
        visit(ctx.expr());
        var value = stack.pop();
        var negated = !boolCast(value);
        stack.push(negated);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void visitFunctionCallExpr(ToyScriptParser.FunctionCallExprContext ctx) {
        var identifier = ctx.ID();
        var scope = registry.getDeclaringScope(identifier);
        var function = registry.read(identifier, scope, Function.class);

        var args = new Object[ctx.expr().size()];
        for (var i = 0; i < ctx.expr().size(); i++) {
            visit(ctx.expr(i));
            args[i] = stack.pop();
        }
        var detached = registry.detachScope(scope);
        var result = function.apply(args);
        detached.reattach();
        stack.push(result);
        return null;
    }

    @Override
    public Void visitFunctionDecl(ToyScriptParser.FunctionDeclContext ctx) {
        var identifier = ctx.ID().get(0);
        var function = new Function<Object[], Object>() {

            private final List<TerminalNode> params = ctx.ID().subList(1, ctx.ID().size());

            @Override
            public Object apply(Object[] args) {
                registry.enterScope();
                var limit = Math.min(params.size(), args.length);
                var i = 0;
                for (; i < limit; i++) registry.declare(params.get(i), args[i]);
                for (; i < params.size(); i++) registry.declare(params.get(i));
                Object result = null;
                for (var statement : ctx.statement()) {
                    visit(statement);
                    if (panicToken.isPresent()) {
                        if (panicToken.top().getType() == ToyScriptLexer.RETURN) {
                            panicToken.pop();
                            result = stack.pop();
                        }
                        break;
                    }
                }
                registry.exitScope();
                return result;
            }

            @Override
            public String toString() {
                return "function(" + params.stream().map(ParseTree::getText).collect(joining(", ")) + ")";
            }
        };
        registry.declare(identifier, function);
        return null;
    }

    @Override
    public Void visitTryStatement(ToyScriptParser.TryStatementContext ctx) {
        visit(ctx.blockStatement());
        if (panicToken.isPresent() && panicToken.top().getType() == ToyScriptLexer.THROW) {
            panicToken.pop();
            var err = stack.pop();
            registry.enterScope();
            var errId = ctx.ID();
            if (errId != null) {
                registry.declare(errId, err);
            }
            for (var statement : ctx.statement()) {
                visit(statement);
                if (panicToken.isPresent()) break;
            }
            registry.exitScope();
        }
        return null;
    }

    @Override
    public Void visitThrowStatement(ToyScriptParser.ThrowStatementContext ctx) {
        var expression = ctx.expr();
        if (expression == null) stack.push(null);
        else visit(expression);
        panicToken.push(ctx.THROW().getSymbol());
        return null;
    }

    public Object getResult() {
        return result;
    }
}
