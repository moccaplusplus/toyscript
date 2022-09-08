package lang.toyscript.engine.runtime.stack;

import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PanicChannel {

    public static final Logger LOGGER = LoggerFactory.getLogger(PanicChannel.class);

    public static PanicChannel create() {
        return LOGGER.isDebugEnabled() ? new DebugPanicChannel() : new PanicChannel();
    }

    private Token token;

    PanicChannel() {
    }

    public boolean isPresent() {
        return token != null;
    }

    public Token pop() {
        var token = this.token;
        this.token = null;
        return token;
    }

    public Token top() {
        return token;
    }

    public void push(Token token) {
        if (isPresent()) {
            throw new IllegalStateException("Two panic tokens - this should never happen");
        }
        this.token = token;
    }
}
