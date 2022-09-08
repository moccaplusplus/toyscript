package lang.toyscript.engine.runtime.registry;

import org.antlr.v4.runtime.tree.TerminalNode;

import static lang.toyscript.engine.runtime.TypeUtils.ellipsize;

public class DebugRegistry implements Registry {

    private final Registry wrapped;

    DebugRegistry(Registry wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void declare(TerminalNode id, Object value) {
        wrapped.declare(id, value);
        LOGGER.debug("Declare {}={} [scope={}]", id.getText(), ellipsize(value), getCurrentScope());
    }

    @Override
    public Object read(TerminalNode id, int scope) {
        var value = wrapped.read(id, scope);
        LOGGER.debug("Read {}={} [scope={}]", id.getText(), ellipsize(value), getCurrentScope());
        return value;
    }

    @Override
    public void assign(TerminalNode id, Object value, int scope) {
        wrapped.assign(id, value, scope);
        LOGGER.debug("Assign {}={} [scope={}]", id.getText(), ellipsize(value), getCurrentScope());
    }

    @Override
    public int getDeclaringScope(TerminalNode id) {
        return wrapped.getDeclaringScope(id);
    }

    @Override
    public int getCurrentScope() {
        return wrapped.getCurrentScope();
    }

    @Override
    public void enterScope() {
        var scopeBefore = getCurrentScope();
        wrapped.enterScope();
        LOGGER.debug("Enter scope {} ----> {}", scopeBefore, getCurrentScope());
    }

    @Override
    public void exitScope() {
        var scopeBefore = getCurrentScope();
        wrapped.exitScope();
        LOGGER.debug("Exit scope {} <---- {}", getCurrentScope(), scopeBefore);
    }

    @Override
    public Detached detachScope(int scope) {
        var count = Math.max(0, getCurrentScope() - scope);
        var detached = wrapped.detachScope(scope);
        LOGGER.debug("Detach scopes after scope={} [detached-count={}; scope={}]", scope, count, getCurrentScope());
        return () -> {
            if (scope != getCurrentScope()) {
                throw new IllegalStateException("Reattach in different scope - it should never happen!");
            }
            detached.reattach();
            LOGGER.debug("Reattach scopes after scope={} [reattached-count={}; scope={}]", scope, count, getCurrentScope());
        };
    }
}
