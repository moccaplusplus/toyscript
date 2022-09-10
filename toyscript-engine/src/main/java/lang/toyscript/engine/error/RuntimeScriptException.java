package lang.toyscript.engine.error;

import org.antlr.v4.runtime.Token;

import javax.script.ScriptException;

public class RuntimeScriptException extends RuntimeException {

    private final int lineNumber;

    private final int columnNumber;

    public RuntimeScriptException(String message, int lineNumber, int columnNumber) {
        super(message);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public RuntimeScriptException(String message, Token token) {
        this(message, token.getLine(), token.getCharPositionInLine());
    }

    public ScriptException checked() {
        return new ScriptException(getMessage(), "script", lineNumber, columnNumber);
    }
}
