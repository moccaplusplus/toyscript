package lang.toyscript.console;

import lang.toyscript.engine.ToyScriptEngineFactory;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ToyScriptConsole {

    public static void main(String[] args) throws IOException, ScriptException {
        var console = new ToyScriptConsole();
        console.parseArgs(args);
        console.start();
    }

    private final ScriptEngineFactory factory = new ToyScriptEngineFactory();

    private boolean interactive = true;
    private String encoding;
    private Path path;
    private boolean help;

    private void parseArgs(String[] args) {
        for (var i = 0; i < args.length; i++) {
            var arg = args[i];
            if ("-h".equals(arg) || "--help".equals(arg)) {
                help = true;
                break;
            }
            if ("-i".equals(arg) || "--interactive".equals(arg)) interactive = true;
            else if ("-e".equals(arg) || "--encoding".equals(arg)) encoding = args[++i];
            else path = Paths.get(arg);
        }
    }

    public void start() throws ScriptException, IOException {
        if (help) printHelp();
        else if (interactive) consoleLoop();
        else if (path != null) execute(path, Charset.forName(encoding));
        else printHelp();
    }

    private void execute(Path path, Charset charset) throws IOException, ScriptException {
        var engine = factory.getScriptEngine();
        engine.eval(Files.newBufferedReader(path, charset));
    }

    private void consoleLoop() throws IOException, ScriptException {
        var engine = factory.getScriptEngine();
        var context = engine.getContext();
        var reader = new BufferedReader(context.getReader());
        var writer = new BufferedWriter(context.getWriter());
        writer.write(factory.getEngineName() + " " + factory.getEngineVersion());
        writer.newLine();
        writer.write("Type /quit to exit");
        writer.newLine();
        while (true) {
            writer.write("ToyScript> ");
            writer.flush();
            var statement = reader.readLine();
            if (statement.trim().equals("/quit")) break;
            var result = engine.eval(statement);
            writer.write(String.valueOf(result));
            writer.newLine();
            writer.flush();
        }
    }

    private void printHelp() {
        System.out.println("Usage: toyscript [-i] [-h] /path/to/script.toys [-e utf-8]");
        System.out.println("Options:");
        System.out.println("    -e, --encoding         Optional. Script file encoding. Default: utf-8.");
        System.out.println("    -i, --interactive      Interactive mode. Path to file not needed.");
        System.out.println("    -h, --help             Prints help.");
    }
}
