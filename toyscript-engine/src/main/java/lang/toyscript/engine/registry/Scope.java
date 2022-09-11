package lang.toyscript.engine.registry;

import lang.toyscript.engine.error.SignalException;
import lang.toyscript.engine.lib.StandardLib;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import java.util.HashMap;
import java.util.Map;

import static javax.script.ScriptContext.ENGINE_SCOPE;
import static javax.script.ScriptContext.GLOBAL_SCOPE;

public class Scope {

    static Logger LOGGER = LoggerFactory.getLogger(Scope.class);

    public static Scope root(ScriptContext scriptContext) {
        var bindings = StandardLib.createBindings(scriptContext);
        var scope = LOGGER.isDebugEnabled() ? new DebugScope(bindings) : new Scope(bindings);
        bindings = scriptContext.getBindings(GLOBAL_SCOPE);
        if (bindings != null) scope = scope.createChild(bindings);
        bindings = scriptContext.getBindings(ENGINE_SCOPE);
        if (bindings != null) scope = scope.createChild(bindings);
        return scope;
    }

    private final Map<String, Object> bindings;
    private final Scope parent;

    Scope(Map<String, Object> bindings) {
        this(null, bindings);
    }

    Scope(Scope parent, Map<String, Object> bindings) {
        this.parent = parent;
        this.bindings = bindings;
    }

    public void declare(TerminalNode id) {
        declare(id, null);
    }

    public void declare(TerminalNode id, Object value) {
        var name = id.getText();
        if (bindings.containsKey(name)) {
            throw new SignalException.Throw(id.getSymbol(),
                    "Identifier " + name + " already declared in current scope");
        }
        bindings.put(name, value);
    }

    public Scope getDeclaringScope(TerminalNode id) {
        var name = id.getText();
        var scope = this;
        while (scope != null) {
            if (scope.bindings.containsKey(name)) return scope;
            scope = scope.parent;
        }
        throw new SignalException.Throw(id.getSymbol(), "Identifier " + name + " is not declared");
    }

    public void write(TerminalNode id, Object value) {
        var name = id.getText();
        getDeclaringScope(id).bindings.put(name, value);
    }

    public Object read(TerminalNode id) {
        var name = id.getText();
        return getDeclaringScope(id).bindings.get(name);
    }

    public Scope getParent() {
        return parent;
    }

    public Scope createChild() {
        return createChild(new HashMap<>());
    }

    Scope createChild(Map<String, Object> bindings) {
        return new Scope(this, bindings);
    }
}
