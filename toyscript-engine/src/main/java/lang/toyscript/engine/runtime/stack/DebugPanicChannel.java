package lang.toyscript.engine.runtime.stack;

import org.antlr.v4.runtime.Token;

public class DebugPanicChannel extends PanicChannel {

    DebugPanicChannel() {
        super();
    }

    @Override
    public void push(Token token) {
        super.push(token);
        LOGGER.debug("Push <token={}>", token.getText());
    }

    @Override
    public Token pop() {
        var token = super.pop();
        LOGGER.debug("Pop <token={}>", token.getText());
        return token;
    }
}
