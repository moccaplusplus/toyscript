package lang.toyscript.engine.lib;

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

import static lang.toyscript.engine.visitor.TypeUtils.typeName;

public interface StandardLib {

    JavaCall readFile = new JavaCall("path", args -> {
        if (args[0] == null) throw new IllegalStateException("Path to file cannot be null");
        var path = Paths.get(String.valueOf(args[0]));
        var lines = Files.readAllLines(path);
        return String.join("\n", lines);
    });

    JavaCall writeFile = new JavaCall(new String[]{"path, text"}, args -> {
        if (args[0] == null) throw new IllegalStateException("Path to file cannot be null");
        if (args[1] == null) throw new IllegalStateException("Text to write cannot be null ");
        var path = Paths.get(String.valueOf(args[0]));
        var text = String.valueOf(args[1]);
        Files.writeString(path, text);
        return null;
    });

    JavaCall length = new JavaCall("arg", args -> {
        if (args[0] instanceof List<?> list) return list.size();
        if (args[0] instanceof Map<?, ?> map) return map.size();
        if (args[0] instanceof String str) return str.length();
        return null;
    });

    JavaCall typeof = new JavaCall("obj", args -> typeName(args[0]));

    JavaCall keys = new JavaCall("obj", args -> {
        if (args[0] instanceof Map<?, ?> m) {
            return m.keySet().stream().map(String::valueOf).toList();
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
