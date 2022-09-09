package lang.toyscript.console;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ToyScriptConsole {

    public static void main(String[] args) throws IOException, ScriptException {
        var console = new ToyScriptConsole();
        console.parseArgs(args);
        console.start();
    }

    private boolean interactive;
    private String encoding;
    private String path;
    private boolean help;
    private final ScriptEngine engine;

    ToyScriptConsole() {
        engine = new ScriptEngineManager().getEngineByName("ToyScript");
    }

    void parseArgs(String[] args) {
        for (var i = 0; i < args.length; i++) {
            var arg = args[i];
            if ("-h".equals(arg) || "--help".equals(arg)) {
                help = true;
                break;
            }
            if ("-i".equals(arg) || "--interactive".equals(arg)) interactive = true;
            else if ("-e".equals(arg) || "--encoding".equals(arg)) encoding = args[++i];
            else path = arg;
        }
    }

    void start() throws ScriptException, IOException {
        if (help) printHelp();
        else if (path != null) execute(Paths.get(path), Charset.forName(encoding));
        else if (interactive) consoleLoop();
        else printHelp();
    }

    private void execute(Path path, Charset charset) throws IOException, ScriptException {
        engine.eval(Files.newBufferedReader(path, charset));
    }

    private void consoleLoop() throws IOException {
        var quitCommands = List.of("/q", "/quit");
        var factory = engine.getFactory();
        var context = engine.getContext();
        var reader = new BufferedReader(context.getReader());
        var writer = new BufferedWriter(context.getWriter());
        writer.write(factory.getEngineName() + " " + factory.getEngineVersion());
        writer.newLine();
        writer.write("Hint: type /q or /quit to exit the console");
        writer.newLine();
        while (true) {
            writer.write("ToyScript> ");
            writer.flush();
            var statement = reader.readLine();
            if (quitCommands.contains(statement.trim())) break;
            try {
                var result = engine.eval(statement);
                if (result != null) {
                    writer.write(String.valueOf(result));
                    writer.newLine();
                }
            } catch (Exception e) {
                writer.write("[ERROR: " + e.getClass().getSimpleName() + "] " + e.getMessage());
                writer.newLine();
            }
            writer.flush();
        }
    }

    private void printHelp() {
        System.out.println("Usage: toyscript [-i] [-h] /path/to/script.toys [-e utf-8]");
        System.out.println("Options:");
        var format = "%6s, %-18s %s %n";
        System.out.printf(format, "-e", "--encoding", "Optional. Script file encoding. Default: utf-8.");
        System.out.printf(format, "-i", "--interactive", "Interactive mode. Path to file not needed.");
        System.out.printf(format, "-h", "--help", "Prints help.");
    }
}
