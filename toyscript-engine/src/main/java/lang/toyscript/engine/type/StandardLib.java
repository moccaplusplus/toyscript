package lang.toyscript.engine.type;

import javax.script.ScriptContext;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface StandardLib {

    JavaCall readFile = new JavaCall("function(path)", args -> {
        if (args.length == 0) throw new IllegalStateException("Path to file cannot be null");
        var path = Paths.get(String.valueOf(args[0]));
        var lines = Files.readAllLines(path);
        return String.join("\n", lines);
    });

    JavaCall writeFile = new JavaCall("function(path, text)", args -> {
        if (args.length == 0) throw new IllegalStateException("Path to file cannot be null");
        if (args.length == 1) throw new IllegalStateException("Text to write cannot be null ");
        var path = Paths.get(String.valueOf(args[0]));
        var text = String.valueOf(args[1]);
        Files.writeString(path, text);
        return null;
    });

    JavaCall length = new JavaCall("function(arg)", args -> {
        if (args[0] instanceof List<?> list) return list.size();
        if (args[0] instanceof Map<?, ?> map) return map.size();
        if (args[0] instanceof String str) return str.length();
        return null;
    });

    JavaCall typeof = new JavaCall("function(v)", args -> {
        if (args.length == 0) return "null";
        if (args[0] == null) return "null";
        if (args[0] instanceof Boolean) return "boolean";
        if (args[0] instanceof Integer) return "integer";
        if (args[0] instanceof Float) return "float";
        if (args[0] instanceof String) return "string";
        if (args[0] instanceof List<?>) return "array";
        if (args[0] instanceof Map<?, ?>) return "struct";
        if (args[0] instanceof Function<?, ?>) return "function";
        return "unknown";
    });

    JavaCall keys = new JavaCall("function(s)", args -> {
        if (args.length > 0 && args[0] instanceof Map<?, ?> map) {
            return map.keySet().stream().map(String::valueOf).toList();
        }
        return null;
    });

    static JavaCall printLine(Writer writer) {
        return new JavaCall("function(line)", args -> {
            if (args.length > 0) writer.write(String.valueOf(args[0]));
            writer.write(System.lineSeparator());
            writer.flush();
            return null;
        });
    }

    static Function<Object[], Object> readLine(Reader reader) {
        var buffered = reader instanceof BufferedReader b ?
                b : new BufferedReader(reader);
        return new JavaCall("function()", args -> buffered.readLine());
    }

    static Map<String, Object> createBindings(ScriptContext scriptContext) {
        return createBindings(scriptContext.getReader(), scriptContext.getWriter());
    }

    static Map<String, Object> createBindings(Reader reader, Writer writer) {
        var m = new HashMap<String, Object>();
        m.put("read", readLine(reader));
        m.put("print", printLine(writer));
        m.put("readFile", readFile);
        m.put("writeFile", writeFile);
        m.put("length", length);
        m.put("typeof", typeof);
        m.put("keys", keys);
        return m;
    }
}
