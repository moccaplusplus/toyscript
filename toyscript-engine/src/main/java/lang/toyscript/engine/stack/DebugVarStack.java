package lang.toyscript.engine.stack;

import static lang.toyscript.engine.visitor.TypeUtils.ellipsize;

public class DebugVarStack extends VarStack {

    DebugVarStack() {
        super();
    }

    @Override
    public void push(Object value) {
        super.push(value);
        LOGGER.debug("Push <{}> [size={}]", ellipsize(value), size());
    }

    @Override
    public Object pop() {
        var value = super.pop();
        LOGGER.debug("Pop <{}> [size={}]", ellipsize(value), size());
        return value;
    }
}

