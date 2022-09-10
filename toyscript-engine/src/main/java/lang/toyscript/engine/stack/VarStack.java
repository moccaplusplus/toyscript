package lang.toyscript.engine.stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class VarStack {

    public static final Logger LOGGER = LoggerFactory.getLogger(VarStack.class);

    public static VarStack create() {
        return LOGGER.isDebugEnabled() ? new DebugVarStack() : new VarStack();
    }

    protected final List<Object> list = new ArrayList<>();

    VarStack() {
    }

    public void push(Object value) {
        list.add(value);
    }

    public Object pop() {
        return list.remove(size() - 1);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void clear() {
        list.clear();
    }
}
