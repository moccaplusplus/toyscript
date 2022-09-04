package lang.toyscript.engine.runtime.scope;

import lang.toyscript.engine.exception.UncheckedScriptException;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.script.ScriptContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ScopedRegister implements Register {

    private final Stack<Map<String, Object>> scopes = new Stack<>();

    private final int rootScope;

    public ScopedRegister(ScriptContext scriptContext) {
        scopes.push(scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE));
        scopes.push(scriptContext.getBindings(ScriptContext.ENGINE_SCOPE));
        rootScope = 1;
    }

    @Override
    public void declare(TerminalNode id) {
        var bindings = scopes.get(getCurrentScope());
        var name = id.getText();
        if (bindings.containsKey(name)) {
            throw new UncheckedScriptException(
                    "Identifier: " + name + " already declared in current scope", id.getSymbol());
        }
        bindings.put(name, null);
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
        scopes.push(new HashMap<>());
    }

    @Override
    public void exitScope() {
        if (getCurrentScope() == rootScope) {
            throw new IllegalStateException("Cannot exit the root scope");
        }
        scopes.pop();
    }
}
