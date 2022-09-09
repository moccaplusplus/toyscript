package lang.toyscript.engine.runtime.registry;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;

import static lang.toyscript.engine.runtime.type.TypeUtils.ensureType;

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

    default <T> T read(TerminalNode id, int scope, Class<T> type) {
        return ensureType(read(id, scope), type, id.getSymbol());
    }

    default Object read(TerminalNode id) {
        return read(id, getDeclaringScope(id));
    }

    default <T> T read(TerminalNode id, Class<T> type) {
        return read(id, getDeclaringScope(id), type);
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
