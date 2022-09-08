package lang.toyscript.engine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.List;

public class ToyScriptEngineFactory implements ScriptEngineFactory {

    @Override
    public String getEngineName() {
        return "ToyScript";
    }

    @Override
    public String getEngineVersion() {
        return "1.0-alpha";
    }

    @Override
    public List<String> getExtensions() {
        return List.of(".toys");
    }

    @Override
    public List<String> getMimeTypes() {
        return List.of("application/vnd.lang.toyscript");
    }

    @Override
    public List<String> getNames() {
        return List.of("ToyScript", "toyscript", "toys");
    }

    @Override
    public String getLanguageName() {
        return "toyscript";
    }

    @Override
    public String getLanguageVersion() {
        return "1.0";
    }

    @Override
    public Object getParameter(String key) {
        return switch (key) {
            case ScriptEngine.ENGINE -> getEngineName();
            case ScriptEngine.ENGINE_VERSION -> getEngineVersion();
            case ScriptEngine.LANGUAGE -> getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION -> getLanguageVersion();
            case ScriptEngine.NAME -> getNames().get(0);
            default -> null;
        };
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        return obj + "." + "m" + "(" + String.join(",", args) + ")";
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "print(\"" + toDisplay + "\")";
    }

    @Override
    public String getProgram(String... statements) {
        return String.join(";\n", statements);
    }

    @Override
    public ToyScriptEngine getScriptEngine() {
        return new ToyScriptEngine(this);
    }
}
