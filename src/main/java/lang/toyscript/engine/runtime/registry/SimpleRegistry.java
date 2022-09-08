package lang.toyscript.engine.runtime.registry;

import lang.toyscript.engine.error.UncheckedScriptException;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.script.ScriptContext;

public class SimpleRegistry implements Registry {

    private final ScriptContext scriptContext;

    SimpleRegistry(ScriptContext scriptContext) {
        this.scriptContext = scriptContext;
    }

    @Override
    public void declare(TerminalNode id, Object value) {
        var bindings = scriptContext.getBindings(getCurrentScope());
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
        return scriptContext.getAttribute(name, scope);
    }

    @Override
    public void assign(TerminalNode id, Object value, int scope) {
        var name = id.getText();
        scriptContext.setAttribute(name, value, scope);
    }

    @Override
    public int getDeclaringScope(TerminalNode id) {
        var name = id.getText();
        var scope = scriptContext.getAttributesScope(name);
        if (scope == -1) {
            throw new UncheckedScriptException("Identifier: " + name + " is not declared", id.getSymbol());
        }
        return scope;
    }

    @Override
    public int getCurrentScope() {
        return ScriptContext.ENGINE_SCOPE;
    }
}
