package lang.toyscript.engine.error;

import lang.toyscript.parser.ToyScriptLexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import static lang.toyscript.engine.visitor.TypeUtils.errorMsg;

public class SignalException extends RuntimeException {

    public static SignalException wrap(ParserRuleContext ctx, Exception e) {
        return e instanceof SignalException se ?
                se : new SignalException.Throw(ctx.getStart(), errorMsg(e));
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
            super(ToyScriptLexer.BREAK, token, null);
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

    protected SignalException(Token token, Object payload) {
        this(token.getType(), token, payload);
    }

    public int type() {
        return type;
    }

    public int line() {
        return line;
    }

    public int col() {
        return col;
    }

    public Object payload() {
        return payload;
    }
}
