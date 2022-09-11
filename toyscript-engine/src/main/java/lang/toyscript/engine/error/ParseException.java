package lang.toyscript.engine.error;

import javax.script.ScriptException;

public class ParseException extends RuntimeException {

    private final int line;

    private final int column;

    public ParseException(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    public ScriptException checked() {
        return new ScriptException(getMessage(), "script", line, column);
    }
}
