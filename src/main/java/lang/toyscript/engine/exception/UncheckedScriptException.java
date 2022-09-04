package lang.toyscript.engine.exception;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import javax.script.ScriptException;

public class UncheckedScriptException extends RuntimeException {

    private final int lineNumber;

    private final int columnNumber;

    public UncheckedScriptException(String message, int lineNumber, int columnNumber) {
        super(message);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public UncheckedScriptException(String message, Token token) {
        this(message, token.getLine(), token.getCharPositionInLine());
    }

    public ScriptException checked() {
        return new ScriptException(getMessage(), "script", lineNumber, columnNumber);
    }
}
