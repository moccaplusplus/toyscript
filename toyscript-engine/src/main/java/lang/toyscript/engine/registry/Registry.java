package lang.toyscript.engine.registry;

import lang.toyscript.engine.error.SignalException;
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

    void declare(TerminalNode id, Object value) throws SignalException;

    default void declare(TerminalNode id) throws SignalException {
        declare(id, null);
    }

    Object read(TerminalNode id, int scope);

    default Object read(TerminalNode id) throws SignalException {
        return read(id, getDeclaringScope(id));
    }

    default void assign(TerminalNode id, Object value) throws SignalException {
        assign(id, value, getDeclaringScope(id));
    }

    void assign(TerminalNode id, Object value, int scope);

    int getDeclaringScope(TerminalNode id) throws SignalException;

    int getCurrentScope();

    default void enterScope() {
    }

    default void exitScope() {
    }

    default Detached detachScope(int scope) {
        return NO_OP_DETACHED;
    }
}
