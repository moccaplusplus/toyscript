package lang.toyscript.engine.runtime;

import lang.toyscript.engine.runtime.type.JavaCall;

import javax.script.Bindings;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface StandardLibrary {

    JavaCall readFile = new JavaCall("function(path)", args -> {
        var path = Paths.get(String.valueOf(args[0]));
        var lines = Files.readAllLines(path);
        return String.join("\n", lines);
    });

    JavaCall writeFile = new JavaCall("function(path, text)", args -> {
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

    static void initBindings(Bindings bindings, Reader reader, Writer writer) {
        bindings.put("read", readLine(reader));
        bindings.put("print", printLine(writer));
        bindings.put("readFile", readFile);
        bindings.put("writeFile", writeFile);
        bindings.put("length", length);
        bindings.put("typeof", typeof);
        bindings.put("keys", keys);
    }
}
