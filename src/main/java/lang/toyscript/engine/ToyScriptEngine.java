package lang.toyscript.engine;

import lang.toyscript.engine.runtime.ToyScriptProgram;
import lang.toyscript.parser.ToyScriptLexer;
import lang.toyscript.parser.ToyScriptParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.io.IOException;
import java.io.Reader;

public class ToyScriptEngine implements ScriptEngine, Compilable {

    private ScriptContext context;

    private final ToyScriptEngineFactory factory;

    public ToyScriptEngine() {
        this(null);
    }

    public ToyScriptEngine(ToyScriptEngineFactory factory) {
        this.factory = factory;
        context = new SimpleScriptContext();
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return compile(script).eval(context);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return compile(reader).eval(context);
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return compile(script).eval(context);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return compile(reader).eval(context);
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        return compile(script).eval(bindings);
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        return compile(reader).eval(bindings);
    }

    @Override
    public void put(String key, Object value) {
        getBindings(ScriptContext.ENGINE_SCOPE).put(key, value);
    }

    @Override
    public Object get(String key) {
        return getBindings(ScriptContext.ENGINE_SCOPE).get(key);
    }

    @Override
    public Bindings getBindings(int scope) {
        return context.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        context.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptContext getContext() {
        return context;
    }

    @Override
    public void setContext(ScriptContext context) {
        if (context == null) {
            throw new NullPointerException("Context must not be null");
        }
        this.context = context;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        try {
            var input = CharStreams.fromString(script);
            return doCompile(input);
        } catch (ParseCancellationException | RecognitionException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public CompiledScript compile(Reader reader) throws ScriptException {
        try {
            var input = CharStreams.fromReader(reader);
            return doCompile(input);
        } catch (IOException | ParseCancellationException | RecognitionException e) {
            throw new ScriptException(e);
        }
    }

    private ToyScriptProgram doCompile(CharStream input) {

        // custom error listener
        var errorListener = new ParseErrorListener(context.getErrorWriter());

        //  parse tokens
        var lexer = new ToyScriptLexer(input);
        lexer.addErrorListener(errorListener);
        var tokenStream = new CommonTokenStream(lexer);

        // create AST
        var parser = new ToyScriptParser(tokenStream);
        parser.addErrorListener(errorListener);
        var tree = parser.program();

        return new ToyScriptProgram(this, tree);
    }
}
