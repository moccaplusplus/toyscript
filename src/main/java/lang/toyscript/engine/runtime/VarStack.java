package lang.toyscript.engine.runtime;

import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

import static lang.toyscript.engine.runtime.TypeUtils.ensureType;

public class VarStack {

    private final List<Object> list = new ArrayList<>();

    public void push(Object value) {
        list.add(value);
    }

    public Object pop() {
        return list.remove(list.size() - 1);
    }

    public <T> T pop(Class<T> type, Token pos) {
        return ensureType(pop(), type, pos);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
