package lang.toyscript.engine.error;

import org.antlr.v4.runtime.Token;

public class ScriptError extends Error {

    public static Void unexpectedToken(Token token) {
        throw new ScriptError("Unexpected token " + token.getText() + " at line " + token.getLine() +
                " at column " + token.getCharPositionInLine());
    }

    public ScriptError(String message) {
        super(message);
    }
}
