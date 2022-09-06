package lang.toyscript.engine.runtime.registry;

import lang.toyscript.engine.exception.UncheckedScriptException;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.script.ScriptContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopedRegistry implements Registry {

    private final List<Map<String, Object>> scopes = new ArrayList<>();

    public ScopedRegistry(ScriptContext scriptContext) {
        scopes.add(scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE));
        scopes.add(scriptContext.getBindings(ScriptContext.ENGINE_SCOPE));
    }

    @Override
    public void declare(TerminalNode id, Object value) {
        var bindings = scopes.get(getCurrentScope());
        var name = id.getText();
        if (bindings.containsKey(name)) {
            throw new UncheckedScriptException(
                    "Identifier: " + name + " already declared in current scope", id.getSymbol());
        }
        bindings.put(name, value);
    }

    @Override
    public Object read(TerminalNode id, int scope) {
        var name = id.getText();
        return scopes.get(scope).get(name);
    }

    @Override
    public void assign(TerminalNode id, Object value, int scope) {
        var name = id.getText();
        scopes.get(scope).put(name, value);
    }

    @Override
    public int getDeclaringScope(TerminalNode id) {
        var name = id.getText();
        for (var i = getCurrentScope(); i >= 0; i--) {
            var bindings = scopes.get(i);
            if (bindings.containsKey(name)) return i;
        }
        throw new UncheckedScriptException("Identifier: " + name + " is not declared", id.getSymbol());
    }

    @Override
    public int getCurrentScope() {
        return scopes.size() - 1;
    }

    @Override
    public void enterScope() {
        scopes.add(new HashMap<>());
    }

    @Override
    public void exitScope() {
        var scope = getCurrentScope();
        if (scope == 1) {
            throw new IllegalStateException("Cannot remove root scope!");
        }
        scopes.remove(scope);
    }

    @Override
    public Detached detachScope(int scope) {
        int count = scopes.size() - scope - 1;
        if (count <= 0) return NO_OP_DETACHED;
        var toDetach = scopes.subList(scope + 1, scopes.size());
        var detached = new ArrayList<>(toDetach);
        toDetach.clear();
        return () -> scopes.addAll(detached);
    }
}
