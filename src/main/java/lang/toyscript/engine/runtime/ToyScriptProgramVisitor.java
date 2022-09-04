package lang.toyscript.engine.runtime;

import lang.toyscript.engine.runtime.scope.ScopeManager;
import lang.toyscript.parser.ToyScriptBaseVisitor;
import lang.toyscript.parser.ToyScriptLexer;
import lang.toyscript.parser.ToyScriptParser;

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

public class ToyScriptProgramVisitor extends ToyScriptBaseVisitor<Void> {

    private final ScopeManager scopeManager;

    private final Stack<Object> stack = new Stack<>();

    public ToyScriptProgramVisitor(ScopeManager scopeManager) {
        this.scopeManager = scopeManager;
    }

    @Override
    public Void visitVarDecl(ToyScriptParser.VarDeclContext ctx) {
        var identifier = ctx.ID().getText();
        scopeManager.declare(identifier);
        if (ctx.expr() != null) {
            visit(ctx.expr());
            var value = stack.pop();
            scopeManager.setValue(identifier, value);
        }
        return null;
    }

    @Override
    public Void visitBlockStatement(ToyScriptParser.BlockStatementContext ctx) {
        scopeManager.enterScope();
        for (var statement : ctx.statement()) {
            visit(statement);
        }
        scopeManager.exitScope();
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
    public Void visitArithmeticExpr(ToyScriptParser.ArithmeticExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        var value1 = stack.pop();
        var value0 = stack.pop();
        var num0 = numberCast(value0);
        var num1 = numberCast(value1);
        var result = switch (ctx.op.getType()) {
            case ToyScriptLexer.ADD -> numberAdd(num0, num1);
            case ToyScriptLexer.SUB -> numberSub(num0, num1);
            case ToyScriptLexer.MUL -> numberMul(num0, num1);
            case ToyScriptLexer.DIV -> numberDiv(num0, num1);
            case ToyScriptLexer.MOD -> numberMod(num0, num1);
            default -> throw new IllegalStateException("Unexpected OP token: " + ctx.op.getText());
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
            case ToyScriptLexer.EQ -> numberEquals(num0, num1);
            case ToyScriptLexer.NEQ -> !numberEquals(num0, num1);
            case ToyScriptLexer.LT -> numberLessThen(num0, num1);
            case ToyScriptLexer.GT -> numberGreaterThen(num0, num1);
            case ToyScriptLexer.LTE -> !numberGreaterThen(num0, num1);
            case ToyScriptLexer.GTE -> !numberLessThen(num0, num1);
            default -> throw new IllegalStateException("Unexpected OP token: " + ctx.op.getText());
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
    public Void visitStringLiteralExpr(ToyScriptParser.StringLiteralExprContext ctx) {
        var value = ctx.STRING().getText();
        stack.push(value);
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
    public Void visitVarExpr(ToyScriptParser.VarExprContext ctx) {
        var identifier = ctx.ID().getText();
        var value = scopeManager.getValue(identifier);
        stack.push(value);
        return null;
    }

    @Override
    public Void visitNestedExpr(ToyScriptParser.NestedExprContext ctx) {
        visit(ctx.expr());
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
    public Void visitAssignExpr(ToyScriptParser.AssignExprContext ctx) {
        var identifier = ctx.ID().getText();
        var scope = scopeManager.getDeclaringScope(identifier);
        visit(ctx.expr());
        var value = stack.pop();
        scopeManager.setValue(identifier, value, scope);
        return null;
    }

    public Object getValue() {
        return stack.isEmpty() ? null : stack.pop();
    }
}
