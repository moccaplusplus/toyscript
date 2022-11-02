package lang.toyscript.engine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class ToyScriptEngineFactory implements ScriptEngineFactory {

    private final List<String> names;
    private final String version;
    private final List<String> extensions;
    private final List<String> mimeTypes;

    public ToyScriptEngineFactory() {
        var properties = new Properties();
        try {
            properties.load(ToyScriptEngineFactory.class.getResourceAsStream("/toyscript.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        names = Stream.of(properties.getProperty("toyscript.names").split(",")).map(String::trim).toList();
        version = properties.getProperty("toyscript.version");
        extensions = List.of(properties.getProperty("toyscript.extension"));
        mimeTypes = List.of(properties.getProperty("toyscript.mimetype"));
    }

    @Override
    public String getEngineName() {
        return getNames().get(0);
    }

    @Override
    public String getEngineVersion() {
        return version;
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getLanguageName() {
        return getNames().get(0);
    }

    @Override
    public String getLanguageVersion() {
        return version;
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
