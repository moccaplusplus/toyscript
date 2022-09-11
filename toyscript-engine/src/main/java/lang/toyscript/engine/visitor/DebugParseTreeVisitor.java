package lang.toyscript.engine.visitor;

import lang.toyscript.engine.registry.Scope;
import lang.toyscript.engine.stack.VarStack;
import lang.toyscript.parser.ToyScriptParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.function.Function;

public class DebugParseTreeVisitor extends ParseTreeVisitor {

    DebugParseTreeVisitor(VarStack stack, Scope scope) {
        super(stack, scope);
    }

    @Override
    public Void visitProgram(ToyScriptParser.ProgramContext ctx) {
        return logVisit("program", super::visitProgram, ctx);
    }

    @Override
    public Void visitReturnExitClause(ToyScriptParser.ReturnExitClauseContext ctx) {
        return logVisit("returnExitClause", super::visitReturnExitClause, ctx);
    }

    @Override
    public Void visitStatement(ToyScriptParser.StatementContext ctx) {
        return logVisit("statement", super::visitStatement, ctx);
    }

    @Override
    public Void visitArrayDefExpr(ToyScriptParser.ArrayDefExprContext ctx) {
        return logVisit("arrayDefExpr", super::visitArrayDefExpr, ctx);
    }

    @Override
    public Void visitIndexAccessExpr(ToyScriptParser.IndexAccessExprContext ctx) {
        return logVisit("arrayDefExpr", super::visitIndexAccessExpr, ctx);
    }

    @Override
    public Void visitIndexAssignExpr(ToyScriptParser.IndexAssignExprContext ctx) {
        return logVisit("arrayDefExpr", super::visitIndexAssignExpr, ctx);
    }

    @Override
    public Void visitMemberAccessExpr(ToyScriptParser.MemberAccessExprContext ctx) {
        return logVisit("memberAccessExpr", super::visitMemberAccessExpr, ctx);
    }

    @Override
    public Void visitMemberAssignExpr(ToyScriptParser.MemberAssignExprContext ctx) {
        return logVisit("memberAssignExpr", super::visitMemberAssignExpr, ctx);
    }

    @Override
    public Void visitArrayInitExpr(ToyScriptParser.ArrayInitExprContext ctx) {
        return logVisit("arrayInitExpr", super::visitArrayInitExpr, ctx);
    }

    @Override
    public Void visitStructInitExpr(ToyScriptParser.StructInitExprContext ctx) {
        return logVisit("structInitExpr", super::visitStructInitExpr, ctx);
    }

    @Override
    public Void visitVarDecl(ToyScriptParser.VarDeclContext ctx) {
        return logVisit("varDecl", super::visitVarDecl, ctx);
    }

    @Override
    public Void visitBlockStatement(ToyScriptParser.BlockStatementContext ctx) {
        return logVisit("blockStatement", super::visitBlockStatement, ctx);
    }

    @Override
    public Void visitIfStatement(ToyScriptParser.IfStatementContext ctx) {
        return logVisit("ifStatement", super::visitIfStatement, ctx);
    }

    @Override
    public Void visitWhileStatement(ToyScriptParser.WhileStatementContext ctx) {
        return logVisit("whileStatement", super::visitWhileStatement, ctx);
    }

    @Override
    public Void visitLoopExitClause(ToyScriptParser.LoopExitClauseContext ctx) {
        return logVisit("loopExitClause", super::visitLoopExitClause, ctx);
    }

    @Override
    public Void visitExprStatement(ToyScriptParser.ExprStatementContext ctx) {
        return logVisit("exprStatement", super::visitExprStatement, ctx);
    }

    @Override
    public Void visitStringLiteralExpr(ToyScriptParser.StringLiteralExprContext ctx) {
        return logVisit("stringLiteralExpr", super::visitStringLiteralExpr, ctx);
    }

    @Override
    public Void visitAndOrExpr(ToyScriptParser.AndOrExprContext ctx) {
        return logVisit("andOrExpr", super::visitAndOrExpr, ctx);
    }

    @Override
    public Void visitAssignExpr(ToyScriptParser.AssignExprContext ctx) {
        return logVisit("assignExpr", super::visitAssignExpr, ctx);
    }

    @Override
    public Void visitIncrDecrExpr(ToyScriptParser.IncrDecrExprContext ctx) {
        return logVisit("incrDecrExpr", super::visitIncrDecrExpr, ctx);
    }

    @Override
    public Void visitNullLiteralExpr(ToyScriptParser.NullLiteralExprContext ctx) {
        return logVisit("nullLiteralExpr", super::visitNullLiteralExpr, ctx);
    }

    @Override
    public Void visitIntLiteralExpr(ToyScriptParser.IntLiteralExprContext ctx) {
        return logVisit("intLiteralExpr", super::visitIntLiteralExpr, ctx);
    }

    @Override
    public Void visitMulDivModExpr(ToyScriptParser.MulDivModExprContext ctx) {
        return logVisit("mulDivModExpr", super::visitMulDivModExpr, ctx);
    }

    @Override
    public Void visitCompareExpr(ToyScriptParser.CompareExprContext ctx) {
        return logVisit("compareExpr", super::visitCompareExpr, ctx);
    }

    @Override
    public Void visitBooleanLiteralExpr(ToyScriptParser.BooleanLiteralExprContext ctx) {
        return logVisit("booleanLiteralExpr", super::visitBooleanLiteralExpr, ctx);
    }

    @Override
    public Void visitFloatLiteralExpr(ToyScriptParser.FloatLiteralExprContext ctx) {
        return logVisit("floatLiteralExpr", super::visitFloatLiteralExpr, ctx);
    }

    @Override
    public Void visitEqualCheckExpr(ToyScriptParser.EqualCheckExprContext ctx) {
        return logVisit("equalCheckExpr", super::visitEqualCheckExpr, ctx);
    }

    @Override
    public Void visitIdentifierExpr(ToyScriptParser.IdentifierExprContext ctx) {
        return logVisit("varExpr", super::visitIdentifierExpr, ctx);
    }

    @Override
    public Void visitNestedExpr(ToyScriptParser.NestedExprContext ctx) {
        return logVisit("nestedExpr", super::visitNestedExpr, ctx);
    }

    @Override
    public Void visitAddSubExpr(ToyScriptParser.AddSubExprContext ctx) {
        return logVisit("addSubExpr", super::visitAddSubExpr, ctx);
    }

    @Override
    public Void visitUnaryMinusExpr(ToyScriptParser.UnaryMinusExprContext ctx) {
        return logVisit("unaryMinusExpr", super::visitUnaryMinusExpr, ctx);
    }

    @Override
    public Void visitNegationExpr(ToyScriptParser.NegationExprContext ctx) {
        return logVisit("negationExpr", super::visitNegationExpr, ctx);
    }

    @Override
    public Void visitFunctionCallExpr(ToyScriptParser.FunctionCallExprContext ctx) {
        return logVisit("functionCallExpr", super::visitFunctionCallExpr, ctx);
    }

    @Override
    public Void visitFunctionDecl(ToyScriptParser.FunctionDeclContext ctx) {
        return logVisit("functionDecl", super::visitFunctionDecl, ctx);
    }

    @Override
    public Void visitTryStatement(ToyScriptParser.TryStatementContext ctx) {
        return logVisit("tryStatement", super::visitTryStatement, ctx);
    }

    @Override
    public Void visitThrowStatement(ToyScriptParser.ThrowStatementContext ctx) {
        return logVisit("throwStatement", super::visitThrowStatement, ctx);
    }

    private <T extends ParserRuleContext, R> R logVisit(String rule, Function<T, R> delegate, T ctx) {
        var start = ctx.getStart();
        var stop = ctx.getStop();
        LOGGER.debug("Enter {} [line={}; col={}]", rule, start.getLine(), start.getCharPositionInLine());
        var result = delegate.apply(ctx);
        LOGGER.debug("Exit {} [line={}; col={}]", rule, stop.getLine(), stop.getCharPositionInLine());
        return result;
    }
}
