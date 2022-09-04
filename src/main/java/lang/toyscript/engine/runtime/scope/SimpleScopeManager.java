package lang.toyscript.engine.runtime.scope;

import javax.script.ScriptContext;

public class SimpleScopeManager implements ScopeManager {

    private final ScriptContext scriptContext;

    public SimpleScopeManager(ScriptContext scriptContext) {
        this.scriptContext = scriptContext;
    }

    @Override
    public void declare(String identifier) {
        var bindings = scriptContext.getBindings(getCurrentScope());
        if (bindings.containsKey(identifier)) {
            throw new IllegalArgumentException("Identifier: " + identifier + " already declared in current scope");
        }
        bindings.put(identifier, null);
    }

    @Override
    public Object getValue(String identifier) {
        var scope = getDeclaringScope(identifier);
        return scriptContext.getAttribute(identifier, scope);
    }

    @Override
    public void setValue(String identifier, Object value, int scope) {
        scriptContext.setAttribute(identifier, value, scope);
    }

    @Override
    public int getDeclaringScope(String identifier) {
        var scope = scriptContext.getAttributesScope(identifier);
        if (scope == -1) {
            throw new IllegalArgumentException("Identifier: " + identifier + " is not declared");
        }
        return scope;
    }

    @Override
    public int getCurrentScope() {
        return ScriptContext.ENGINE_SCOPE;
    }

    @Override
    public void enterScope() {
    }

    @Override
    public void exitScope() {
    }
}
