package lang.toyscript.engine.registry;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static lang.toyscript.engine.visitor.TypeUtils.ellipsize;

public class DebugScope extends Scope {

    private final int uuid;

    private final AtomicInteger counter;

    DebugScope(Map<String, Object> bindings) {
        super(null, bindings);
        counter = new AtomicInteger(uuid = 0);
    }

    DebugScope(DebugScope parent, Map<String, Object> bindings) {
        super(parent, bindings);
        counter = parent.counter;
        uuid = counter.incrementAndGet();
    }

    @Override
    public void declare(TerminalNode id, Object value) {
        super.declare(id, value);
        LOGGER.debug("Declare {}={} [scope={}]", id.getText(), ellipsize(value), uuid);
    }

    @Override
    public Object read(TerminalNode id) {
        var value = super.read(id);
        LOGGER.debug("Read {}={} [scope={}]", id.getText(), ellipsize(value), uuid);
        return value;
    }

    @Override
    public void write(TerminalNode id, Object value) {
        super.write(id, value);
        LOGGER.debug("Write {}={} [scope={}]", id.getText(), ellipsize(value), uuid);
    }

    @Override
    public Scope getParent() {
        var parent = (DebugScope) super.getParent();
        LOGGER.debug("Parent scope {} <---- {}", parent.uuid, uuid);
        return parent;
    }

    @Override
    Scope createChild(Map<String, Object> bindings) {
        var child = new DebugScope(this, bindings);
        LOGGER.debug("Child scope {} ----> {}", uuid, child.uuid);
        return child;
    }
}
