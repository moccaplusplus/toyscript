package lang.toyscript.engine.runtime.scope;

import javax.script.ScriptContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DynamicScopeManager implements ScopeManager {

    private final Stack<Map<String, Object>> scopeStack = new Stack<>();

    private final int rootScope;

    public DynamicScopeManager(ScriptContext scriptContext) {
        scopeStack.push(scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE));
        scopeStack.push(scriptContext.getBindings(ScriptContext.ENGINE_SCOPE));
        rootScope = 1;
    }

    @Override
    public void declare(String identifier) {
        var bindings = scopeStack.get(getCurrentScope());
        if (bindings.containsKey(identifier)) {
            throw new IllegalArgumentException("Identifier: " + identifier + " already declared in current scope");
        }
        bindings.put(identifier, null);
    }

    @Override
    public Object getValue(String identifier) {
        var scope = getDeclaringScope(identifier);
        return scopeStack.get(scope).get(identifier);
    }

    @Override
    public void setValue(String identifier, Object value, int scope) {
        scopeStack.get(scope).put(identifier, value);
    }

    @Override
    public int getDeclaringScope(String identifier) {
        for (int i = getCurrentScope(); i >= 0; i--) {
            var scope = scopeStack.get(i);
            if (scope.containsKey(identifier)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Identifier: " + identifier + " is not declared");
    }

    @Override
    public int getCurrentScope() {
        return scopeStack.size() - 1;
    }

    @Override
    public void enterScope() {
        scopeStack.push(new HashMap<>());
    }

    @Override
    public void exitScope() {
        if (getCurrentScope() == rootScope) {
            throw new IllegalStateException("Cannot exit the root scope");
        }
        scopeStack.pop();
    }
}
