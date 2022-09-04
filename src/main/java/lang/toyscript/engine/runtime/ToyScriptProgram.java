package lang.toyscript.engine.runtime;

import lang.toyscript.engine.ToyScriptEngine;
import lang.toyscript.engine.runtime.scope.SimpleScopeManager;
import lang.toyscript.parser.ToyScriptParser;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class ToyScriptProgram extends CompiledScript {

    private final ToyScriptEngine engine;

    private final ToyScriptParser.ProgramContext tree;

    public ToyScriptProgram(ToyScriptEngine engine, ToyScriptParser.ProgramContext tree) {
        this.engine = engine;
        this.tree = tree;
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptException {
        var scopeManager = new SimpleScopeManager(context);
        var visitor = new ToyScriptProgramVisitor(scopeManager);
        try {
            visitor.visit(tree);
        } catch (Exception e) {
            throw new ScriptException(e);
        }
        return visitor.getValue();
    }

    @Override
    public Object eval(Bindings bindings) throws ScriptException {
        var engineContext = getEngine().getContext();
        var localContext = new SimpleScriptContext();
        localContext.setReader(engineContext.getReader());
        localContext.setWriter(engineContext.getWriter());
        localContext.setErrorWriter(engineContext.getErrorWriter());
        localContext.setBindings(engineContext.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
        localContext.setBindings(bindings == null ? engine.createBindings() : bindings, ScriptContext.ENGINE_SCOPE);
        return eval(localContext);
    }

    @Override
    public ScriptEngine getEngine() {
        return engine;
    }
}
