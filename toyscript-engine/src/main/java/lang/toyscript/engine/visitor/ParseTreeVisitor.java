package lang.toyscript.engine.visitor;

import lang.toyscript.engine.error.SignalException;
import lang.toyscript.engine.registry.Scope;
import lang.toyscript.engine.stack.VarStack;
import lang.toyscript.parser.ToyScriptLexer;
import lang.toyscript.parser.ToyScriptParser;
import lang.toyscript.parser.ToyScriptVisitor;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
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
import static lang.toyscript.engine.error.ScriptError.unexpectedToken;
import static lang.toyscript.engine.visitor.Types.addExpr;
import static lang.toyscript.engine.visitor.Types.boolCast;
import static lang.toyscript.engine.visitor.Types.divideExpr;
import static lang.toyscript.engine.visitor.Types.ensureStructKey;
import static lang.toyscript.engine.visitor.Types.equalsExpr;
import static lang.toyscript.engine.visitor.Types.greaterThenExpr;
import static lang.toyscript.engine.visitor.Types.lessThenExpr;
import static lang.toyscript.engine.visitor.Types.moduloExpr;
import static lang.toyscript.engine.visitor.Types.multiplyExpr;
import static lang.toyscript.engine.visitor.Types.numberCast;
import static lang.toyscript.engine.visitor.Types.subtractExpr;
import static lang.toyscript.engine.visitor.Types.unaryMinExpr;

public class ParseTreeVisitor extends AbstractParseTreeVisitor<Void> implements ToyScriptVisitor<Void> {

    static final Logger LOGGER = LoggerFactory.getLogger(ParseTreeVisitor.class);

    public static ParseTreeVisitor create(ScriptContext scriptContext) {
        var stack = VarStack.create();
        var scope = Scope.root(scriptContext);
        return LOGGER.isDebugEnabled() ?
                new DebugParseTreeVisitor(stack, scope) :
                new ParseTreeVisitor(stack, scope);
    }

    private final VarStack stack;

    private Scope scope;

    private boolean lastStatement;

    ParseTreeVisitor(VarStack stack, Scope scope) {
        this.stack = stack;
        this.scope = scope;
    }

    @Override
    public Void visitProgram(ToyScriptParser.ProgramContext ctx) {
        for (int i = 0, count = ctx.statement().size(); i < count; i++) {
            var statement = ctx.statement(i);
            lastStatement = i == count - 1 && statement.exprStatement() != null;
            try {
                visit(statement);
            } catch (SignalException.Exit e) {
                stack.push(e.payload());
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitReturnExitClause(ToyScriptParser.ReturnExitClauseContext ctx) {
        Object payload = null;
        if (ctx.expr() != null) {
            visit(ctx.expr());
            payload = stack.pop();
        }
        switch (ctx.op.getType()) {
            case ToyScriptLexer.RETURN -> throw new SignalException.Return(ctx.op, payload);
            case ToyScriptLexer.EXIT -> throw new SignalException.Exit(ctx.op, payload);
        }
        return null;
    }

    @Override
    public Void visitStatement(ToyScriptParser.StatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitArrayDefExpr(ToyScriptParser.ArrayDefExprContext ctx) {
        visit(ctx.expr());
        try {
            var size = numberCast(stack.pop()).intValue();
            var value = Arrays.asList(new Object[size]);
            stack.push(value);
        } catch (Exception e) {
            throw SignalException.wrap(ctx, e);
        }
        return null;
    }

    @Override
    public Void visitIndexAccessExpr(ToyScriptParser.IndexAccessExprContext ctx) {

        var arrExpr = ctx.expr(0);
        visit(arrExpr);
        var obj = stack.pop();

        var indExpr = ctx.expr(1);
        visit(indExpr);
        var key = stack.pop();

        try {
            if (obj instanceof List<?> arr) {
                var index = numberCast(key).intValue();
                var value = arr.get(index);
                stack.push(value);
            } else if (obj instanceof String str) {
                var index = numberCast(key).intValue();
                var value = String.valueOf(str.charAt(index));
                stack.push(value);
            } else {
                throw SignalException.typeMismatch(obj, arrExpr.getStart(), List.class, String.class);
            }
        } catch (Exception e) {
            throw SignalException.wrap(ctx, e);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Void visitIndexAssignExpr(ToyScriptParser.IndexAssignExprContext ctx) {
        var arrExpr = ctx.expr(0);
        visit(arrExpr);
        var obj = stack.pop();

        var indExpr = ctx.expr(1);
        visit(indExpr);
        var key = stack.pop();

        var valExpr = ctx.expr(2);
        visit(valExpr);
        var value = stack.pop();

        if (obj instanceof List arr) {
            var index = numberCast(key).intValue();
            try {
                arr.set(index, value);
                stack.push(value);
            } catch (Exception e) {
                throw SignalException.wrap(ctx, e);
            }
        } else {
            throw SignalException.typeMismatch(obj, arrExpr.getStart(), List.class);
        }
        return null;
    }

    @Override
    public Void visitMemberAccessExpr(ToyScriptParser.MemberAccessExprContext ctx) {
        var mapExpr = ctx.expr();
        visit(mapExpr);
        var obj = stack.pop();

        if (obj instanceof Map<?, ?> map) {
            var key = ensureStructKey(map, ctx.ID());
            try {
                var value = map.get(key);
                stack.push(value);
            } catch (Exception e) {
                throw SignalException.wrap(ctx, e);
            }
        } else {
            throw SignalException.typeMismatch(obj, mapExpr.getStart(), Map.class);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Void visitMemberAssignExpr(ToyScriptParser.MemberAssignExprContext ctx) {

        var mapExpr = ctx.expr(0);
        visit(mapExpr);
        var obj = stack.pop();

        var valExpr = ctx.expr(1);
        visit(valExpr);
        var value = stack.pop();

        if (obj instanceof Map map) {
            var key = ensureStructKey(map, ctx.ID());
            try {
                map.put(key, value);
                stack.push(value);
            } catch (Exception e) {
                throw SignalException.wrap(ctx, e);
            }
        } else {
            throw SignalException.typeMismatch(obj, mapExpr.getStart(), Map.class);
        }

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
        Object value = null;
        if (ctx.expr() != null) {
            visit(ctx.expr());
            value = stack.pop();
        }
        scope.declare(identifier, value);
        return null;
    }

    @Override
    public Void visitBlockStatement(ToyScriptParser.BlockStatementContext ctx) {
        scope = scope.createChild();
        try {
            for (var statement : ctx.statement()) {
                visit(statement);
            }
        } finally {
            scope = scope.getParent();
        }
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
            try {
                visit(ctx.statement());
            } catch (SignalException.Continue e) {
                // just catch :)
            } catch (SignalException.Break e) {
                break;
            }
            visit(ctx.expr());
            value = stack.pop();
            condition = boolCast(value);
        }
        return null;
    }

    @Override
    public Void visitLoopExitClause(ToyScriptParser.LoopExitClauseContext ctx) {
        switch (ctx.op.getType()) {
            case ToyScriptLexer.BREAK -> throw new SignalException.Break(ctx.op);
            case ToyScriptLexer.CONTINUE -> throw new SignalException.Continue(ctx.op);
        }
        return null;
    }

    @Override
    public Void visitExprStatement(ToyScriptParser.ExprStatementContext ctx) {
        visit(ctx.expr());

        // clear stack after standalone expression -
        // but not if it is the last statement in the program
        // (for interactive mode).
        if (!lastStatement) {
            stack.pop();
        }
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
            default -> unexpectedToken(ctx.op);
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitAssignExpr(ToyScriptParser.AssignExprContext ctx) {
        var identifier = ctx.ID();
        var scopeRef = scope.getDeclaringScope(identifier);
        visit(ctx.expr());
        var value = stack.pop();
        scopeRef.write(identifier, value);
        stack.push(value);
        return null;
    }

    @Override
    public Void visitIncrDecrExpr(ToyScriptParser.IncrDecrExprContext ctx) {
        var identifier = ctx.ID();
        var scopeRef = scope.getDeclaringScope(identifier);
        var value = scopeRef.read(identifier);
        stack.push(value);
        value = switch (ctx.op.getType()) {
            case ToyScriptLexer.INCR -> numberCast(value).intValue() + 1;
            case ToyScriptLexer.DECR -> numberCast(value).intValue() - 1;
            default -> unexpectedToken(ctx.op);
        };
        scopeRef.write(identifier, value);
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
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.MUL -> multiplyExpr(value0, value1);
            case ToyScriptLexer.DIV -> divideExpr(value0, value1);
            case ToyScriptLexer.MOD -> moduloExpr(value0, value1);
            default -> unexpectedToken(ctx.op);
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitCompareExpr(ToyScriptParser.CompareExprContext ctx) {
        visit(ctx.expr(0));
        var value0 = stack.pop();
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.LT -> lessThenExpr(value0, value1);
            case ToyScriptLexer.GT -> greaterThenExpr(value0, value1);
            case ToyScriptLexer.LTE -> !greaterThenExpr(value0, value1);
            case ToyScriptLexer.GTE -> !lessThenExpr(value0, value1);
            default -> unexpectedToken(ctx.op);
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
        var value0 = stack.pop();
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.EQ -> equalsExpr(value0, value1);
            case ToyScriptLexer.NEQ -> !equalsExpr(value0, value1);
            default -> unexpectedToken(ctx.op);
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitIdentifierExpr(ToyScriptParser.IdentifierExprContext ctx) {
        var identifier = ctx.ID();
        var value = scope.read(identifier);
        if (value instanceof Function<?, ?>) {
            throw new SignalException.Throw(identifier.getSymbol(),
                    "Function reference cannot be used in expression");
        }
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
        var value0 = stack.pop();
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.PLUS -> addExpr(value0, value1);
            case ToyScriptLexer.MINUS -> subtractExpr(value0, value1);
            default -> unexpectedToken(ctx.op);
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitUnaryMinusExpr(ToyScriptParser.UnaryMinusExprContext ctx) {
        visit(ctx.expr());
        var value = stack.pop();
        var result = unaryMinExpr(value);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Void visitFunctionCallExpr(ToyScriptParser.FunctionCallExprContext ctx) {
        var identifier = ctx.ID();

        var count = ctx.expr().size();
        var args = new Object[count];
        for (var i = 0; i < count; i++) {
            visit(ctx.expr(i));
            args[i] = stack.pop();
        }

        var obj = scope.read(identifier);
        if (obj instanceof Function function) {
            try {
                var result = function.apply(args);
                stack.push(result);
            } catch (Exception e) {
                throw SignalException.wrap(ctx, e);
            }
        } else {
            throw SignalException.typeMismatch(obj, identifier.getSymbol(), Function.class);
        }
        return null;
    }

    @Override
    public Void visitFunctionDecl(ToyScriptParser.FunctionDeclContext ctx) {

        var identifier = ctx.ID().get(0);
        var params = ctx.ID().subList(1, ctx.ID().size());
        var signature = "function(" + params.stream().map(ParseTree::getText).collect(joining(", ")) + ")";
        var enclosingScope = scope;

        scope.declare(identifier, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] args) {

                var callerScope = scope;
                scope = enclosingScope.createChild();

                try {
                    var limit = Math.min(params.size(), args.length);
                    var i = 0;
                    for (; i < limit; i++) scope.declare(params.get(i), args[i]);
                    for (; i < params.size(); i++) scope.declare(params.get(i));
                    for (var statement : ctx.statement()) {
                        try {
                            visit(statement);
                        } catch (SignalException.Return e) {
                            return e.payload();
                        }
                    }
                    return null;
                } finally {
                    scope = callerScope;
                }
            }

            @Override
            public String toString() {
                return signature;
            }
        });

        return null;
    }

    @Override
    public Void visitTryStatement(ToyScriptParser.TryStatementContext ctx) {
        try {
            visit(ctx.blockStatement());
        } catch (SignalException.Throw e) {
            scope = scope.createChild();
            try {
                stack.clear();
                var errId = ctx.ID();
                if (errId != null) {
                    scope.declare(errId, e.payload());
                }
                for (var statement : ctx.statement()) {
                    visit(statement);
                }
            } finally {
                scope = scope.getParent();
            }
        }
        return null;
    }

    @Override
    public Void visitThrowStatement(ToyScriptParser.ThrowStatementContext ctx) {
        Object payload = null;
        if (ctx.expr() != null) {
            visit(ctx.expr());
            payload = stack.pop();
        }
        throw new SignalException.Throw(ctx.THROW().getSymbol(), payload);
    }

    public Object getResult() {
        return stack.isEmpty() ? null : stack.pop();
    }
}
