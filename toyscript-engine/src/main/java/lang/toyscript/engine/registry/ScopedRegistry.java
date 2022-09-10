package lang.toyscript.engine.registry;

import lang.toyscript.engine.error.SignalException;
import lang.toyscript.engine.lib.StandardLib;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.script.ScriptContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static javax.script.ScriptContext.ENGINE_SCOPE;
import static javax.script.ScriptContext.GLOBAL_SCOPE;

public class ScopedRegistry implements Registry {

    private final List<Map<String, Object>> scopes = new ArrayList<>();

    ScopedRegistry(ScriptContext scriptContext) {
        scopes.add(StandardLib.createBindings(scriptContext));
        scopes.addAll(IntStream.of(GLOBAL_SCOPE, ENGINE_SCOPE)
                .mapToObj(scriptContext::getBindings)
                .filter(Objects::nonNull)
                .toList());
    }

    @Override
    public void declare(TerminalNode id, Object value) throws SignalException {
        var bindings = scopes.get(getCurrentScope());
        var name = id.getText();
        if (bindings.containsKey(name)) {
            throw new SignalException.Throw(id.getSymbol(),
                    "Identifier " + name + " already declared in current scope");
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
    public int getDeclaringScope(TerminalNode id) throws SignalException {
        var name = id.getText();
        for (var i = getCurrentScope(); i >= 0; i--) {
            var bindings = scopes.get(i);
            if (bindings.containsKey(name)) return i;
        }
        throw new SignalException.Throw(id.getSymbol(), "Identifier " + name + " is not declared");
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
        scopes.remove(getCurrentScope());
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
