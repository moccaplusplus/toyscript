package lang.toyscript.engine.exception;

import org.antlr.v4.runtime.Token;

public class LoopExitExcetion extends RuntimeException {

    private final Token token;

    public LoopExitExcetion(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
