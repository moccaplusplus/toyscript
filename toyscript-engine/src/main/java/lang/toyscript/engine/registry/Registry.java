package lang.toyscript.engine.registry;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;

public interface Registry {

    Logger LOGGER = LoggerFactory.getLogger(Registry.class);

    static Registry scoped(ScriptContext scriptContext) {
        var registry = new ScopedRegistry(scriptContext);
        return LOGGER.isDebugEnabled() ? new DebugRegistry(registry) : registry;
    }

    static Registry simple(ScriptContext scriptContext) {
        var registry = new SimpleRegistry(scriptContext);
        return LOGGER.isDebugEnabled() ? new DebugRegistry(registry) : registry;
    }

    Detached NO_OP_DETACHED = () -> {
    };

    @FunctionalInterface
    interface Detached {
        void reattach();
    }

    void declare(TerminalNode id, Object value);

    default void declare(TerminalNode id) {
        declare(id, null);
    }

    Object read(TerminalNode id, int scope);

    default Object read(TerminalNode id) {
        return read(id, getDeclaringScope(id));
    }

    default void assign(TerminalNode id, Object value) {
        assign(id, value, getDeclaringScope(id));
    }

    void assign(TerminalNode id, Object value, int scope);

    int getDeclaringScope(TerminalNode id);

    int getCurrentScope();

    default void enterScope() {
    }

    default void exitScope() {
    }

    default Detached detachScope(int scope) {
        return NO_OP_DETACHED;
    }
}
