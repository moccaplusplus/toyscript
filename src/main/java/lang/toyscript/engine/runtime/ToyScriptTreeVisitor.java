package lang.toyscript.engine.runtime;

import lang.toyscript.engine.exception.UncheckedScriptException;
import lang.toyscript.engine.runtime.scope.Register;
import lang.toyscript.engine.runtime.scope.ScopedRegister;
import lang.toyscript.parser.ToyScriptLexer;
import lang.toyscript.parser.ToyScriptParser;
import lang.toyscript.parser.ToyScriptVisitor;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

import javax.script.ScriptContext;
import java.util.Stack;

import static lang.toyscript.engine.runtime.TypeUtils.boolCast;
import static lang.toyscript.engine.runtime.TypeUtils.numberAdd;
import static lang.toyscript.engine.runtime.TypeUtils.numberCast;
import static lang.toyscript.engine.runtime.TypeUtils.numberDiv;
import static lang.toyscript.engine.runtime.TypeUtils.numberEquals;
import static lang.toyscript.engine.runtime.TypeUtils.numberGreaterThen;
import static lang.toyscript.engine.runtime.TypeUtils.numberLessThen;
import static lang.toyscript.engine.runtime.TypeUtils.numberMod;
import static lang.toyscript.engine.runtime.TypeUtils.numberMul;
import static lang.toyscript.engine.runtime.TypeUtils.numberSub;
import static lang.toyscript.engine.runtime.TypeUtils.unaryMin;

public class ToyScriptTreeVisitor extends AbstractParseTreeVisitor<Void> implements ToyScriptVisitor<Void> {

    private final Stack<Object> stack = new Stack<>();

    private final Register register;

    public ToyScriptTreeVisitor(ScriptContext scriptContext) {
        register = new ScopedRegister(scriptContext);
    }

    @Override
    public Void visitProgram(ToyScriptParser.ProgramContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitStatement(ToyScriptParser.StatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Void visitArrayDefExpr(ToyScriptParser.ArrayDefExprContext ctx) {
        visit(ctx.expr());
        var size = numberCast(stack.pop()).intValue();
        var value = new Object[size];
        stack.push(value);
        return null;
    }

    @Override
    public Void visitIndexAccessExpr(ToyScriptParser.IndexAccessExprContext ctx) {
        visit(ctx.expr(0));
        var target = stack.pop();
        if (target instanceof Object[] arr) {
            visit(ctx.expr(1));
            var index = numberCast(stack.pop()).intValue();
            var value = arr[index];
            stack.push(value);
        }
        return null;
    }

    @Override
    public Void visitIndexAssignExpr(ToyScriptParser.IndexAssignExprContext ctx) {
        visit(ctx.expr(0));
        var target = stack.pop();
        if (target instanceof Object[] arr) {
            visit(ctx.expr(1));
            var index = numberCast(stack.pop()).intValue();
            visit(ctx.expr(2));
            var value = stack.pop();
            arr[index] = value;
            stack.push(value);
        }
        return null;
    }

    @Override
    public Void visitArrayInitExpr(ToyScriptParser.ArrayInitExprContext ctx) {
        visit(ctx.exprSeq());
        var size = (Integer) stack.pop();
        var value = new Object[size];
        while (size-- > 0) {
            value[size] = stack.pop();
        }
        stack.push(value);
        return null;
    }

    @Override
    public Void visitVarDecl(ToyScriptParser.VarDeclContext ctx) {
        var identifier = ctx.ID();
        register.declare(identifier);
        if (ctx.expr() != null) {
            visit(ctx.expr());
            var value = stack.pop();
            register.assign(identifier, value, register.getCurrentScope());
        }
        return null;
    }

    @Override
    public Void visitBlockStatement(ToyScriptParser.BlockStatementContext ctx) {
        register.enterScope();
        for (var statement : ctx.statement()) {
            visit(statement);
        }
        register.exitScope();
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
            visit(ctx.expr());
            value = stack.pop();
            condition = boolCast(value);
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
            default -> throw new IllegalStateException("Unexpected OP token: " + ctx.op.getText());
        };
        stack.push(result);
        return null;
    }

    @Override
    public Void visitAssignExpr(ToyScriptParser.AssignExprContext ctx) {
        var identifier = ctx.ID();
        var scope = register.getDeclaringScope(identifier);
        visit(ctx.expr());
        var value = stack.pop();
        register.assign(identifier, value, scope);
        return null;
    }

    @Override
    public Void visitIncrDecrExpr(ToyScriptParser.IncrDecrExprContext ctx) {
        var identifier = ctx.ID();
        var scope = register.getDeclaringScope(identifier);
        var value = register.read(identifier, scope);
        stack.push(value);
        var seed = switch (ctx.op.getType()) {
            case ToyScriptLexer.INCR -> 1;
            case ToyScriptLexer.DECR -> -1;
            default -> throw new UncheckedScriptException("Unexpected token: " + ctx.op.getText(), ctx.op);
        };
        register.assign(identifier, numberCast(value).intValue() + seed, scope);
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
        var value = register.read(identifier);
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

    @Override
    public Void visitExprSeq(ToyScriptParser.ExprSeqContext ctx) {
        for (var expr : ctx.expr()) {
            visit(expr);
        }
        stack.push(ctx.expr().size());
        return null;
    }

    public Object getResult() {
        return stack.isEmpty() ? null : stack.pop();
    }
}
