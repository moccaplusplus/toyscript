package lang.toyscript.engine.lib;

import lang.toyscript.engine.visitor.Types;

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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static lang.toyscript.engine.visitor.Types.typeNames;

public interface StandardLib {

    JavaFunctionCall readFile = new JavaFunctionCall("path", args -> {
        if (args[0] == null) throw new IllegalStateException("Path to file cannot be null");
        var path = Paths.get(String.valueOf(args[0]));
        var lines = Files.readAllLines(path);
        return String.join("\n", lines);
    });

    JavaFunctionCall writeFile = new JavaFunctionCall(new String[]{"path, text"}, args -> {
        if (args[0] == null) throw new IllegalStateException("Path to file cannot be null");
        if (args[1] == null) throw new IllegalStateException("Text to write cannot be null ");
        var path = Paths.get(String.valueOf(args[0]));
        var text = String.valueOf(args[1]);
        Files.writeString(path, text);
        return null;
    });

    JavaFunctionCall length = new JavaFunctionCall("arg", args -> {
        if (args[0] instanceof List<?> list) return list.size();
        if (args[0] instanceof Map<?, ?> map) return map.size();
        if (args[0] instanceof String str) return str.length();
        return null;
    });

    JavaFunctionCall keys = new JavaFunctionCall("obj", args -> {
        if (args[0] instanceof Map<?, ?> m) {
            return m.keySet().stream().map(String::valueOf).toList();
        }
        return null;
    });

    static JavaFunctionCall printLine(Writer writer) {
        return new JavaFunctionCall("function(line)", args -> {
            if (args.length > 0) writer.write(String.valueOf(args[0]));
            writer.write(System.lineSeparator());
            writer.flush();
            return null;
        });
    }

    static Function<Object[], Object> readLine(Reader reader) {
        var buffered = reader instanceof BufferedReader b ?
                b : new BufferedReader(reader);
        return new JavaFunctionCall("function()", args -> buffered.readLine());
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
        m.put("keys", keys);
        m.put("typeof", new JavaFunctionCall("obj", args -> Types.typeName(args[0])));
        m.put("Types", typeNames().stream().collect(toMap(String::toUpperCase, identity())));
        return m;
    }
}
