package lang.toyscript.engine.runtime;

import lang.toyscript.engine.ToyScriptEngine;
import lang.toyscript.engine.exception.UncheckedScriptException;
import lang.toyscript.parser.ToyScriptParser;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ToyScriptProgram extends CompiledScript {

    private final ToyScriptEngine engine;

    private final ToyScriptParser.ProgramContext tree;

    public ToyScriptProgram(ToyScriptEngine engine, ToyScriptParser.ProgramContext tree) {
        this.engine = engine;
        this.tree = tree;
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptException {
        var visitor = new ToyScriptTreeVisitor(context);
        try {
            visitor.visit(tree);
        } catch (UncheckedScriptException e) {
            throw e.checked();
        } catch (RuntimeException e) {
            throw new ScriptException(e);
        }
        return visitor.getResult();
    }

    @Override
    public ScriptEngine getEngine() {
        return engine;
    }
}
