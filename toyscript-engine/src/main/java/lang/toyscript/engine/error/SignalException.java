package lang.toyscript.engine.error;

import lang.toyscript.engine.visitor.Types;
import lang.toyscript.parser.ToyScriptLexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import javax.script.ScriptException;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static lang.toyscript.engine.visitor.Types.errorMsg;
import static lang.toyscript.engine.visitor.Types.typeName;
import static lang.toyscript.parser.ToyScriptLexer.ruleNames;

public class SignalException extends RuntimeException {

    public static SignalException wrap(ParserRuleContext ctx, Exception e) {
        return wrap(ctx.getStart(), e);
    }

    public static SignalException wrap(Token pos, Exception e) {
        return e instanceof SignalException se ?
                se : new SignalException.Throw(pos, errorMsg(e));
    }

    public static SignalException typeMismatch(Object value, Token pos, Class<?>... expected) {
        return new SignalException.Throw(pos, "Expected " +
                Arrays.stream(expected).map(Types::typeName)
                        .map(String::valueOf).collect(joining(", ")) +
                " but was " + Types.typeName(value));
    }

    public static SignalException typeMismatch(Object value, Token pos, Class<?> expected) {
        return new SignalException.Throw(pos, "Expected " + typeName(expected) +
                " but was " + Types.typeName(value));
    }

    public static class Throw extends SignalException {
        public Throw(Token token, Object payload) {
            super(ToyScriptLexer.THROW, token, payload);
        }
    }

    public static class Return extends SignalException {
        public Return(Token token, Object payload) {
            super(ToyScriptLexer.RETURN, token, payload);
        }
    }

    public static class Exit extends SignalException {
        public Exit(Token token, Object payload) {
            super(ToyScriptLexer.EXIT, token, payload);
        }
    }

    public static class Break extends SignalException {
        public Break(Token token) {
            super(ToyScriptLexer.BREAK, token, null);
        }
    }

    public static class Continue extends SignalException {
        public Continue(Token token) {
            super(ToyScriptLexer.CONTINUE, token, null);
        }
    }

    private final int type;
    private final int line;
    private final int col;
    private final Object payload;

    protected SignalException(int type, int line, int col, Object payload) {
        this.type = type;
        this.line = line;
        this.col = col;
        this.payload = payload;
    }

    protected SignalException(int type, Token token, Object payload) {
        this(type, token.getLine(), token.getCharPositionInLine(), payload);
    }

    public Object payload() {
        return payload;
    }

    public ScriptException checked() {
        if (type == ToyScriptLexer.THROW) {
            return new ScriptException(String.valueOf(payload), "script", line, col);
        }
        return new ScriptException(
                "Token <" + ruleNames[type] + "> in illegal position", "script", line, col);
    }
}
