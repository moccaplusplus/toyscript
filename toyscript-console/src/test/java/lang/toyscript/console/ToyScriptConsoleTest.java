package lang.toyscript.console;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

public class ToyScriptConsoleTest {

    @Test
    public void shouldPrintHelp() throws IOException {
        var cmd = new String[]{"java", "-jar", "target/toyscript.jar"};
        var process = Runtime.getRuntime().exec(cmd);
        var in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        var lines = in.lines().map(String::trim).toArray(String[]::new);

        assertThat(lines).isEqualTo(new String[]{
                "Usage: toyscript [-i] [-h] /path/to/script.toys [-e utf-8]",
                "Options:",
                "-e, --encoding         Optional. Script file encoding. Default: utf-8.",
                "-i, --interactive      Interactive mode. Path to file not needed.",
                "-h, --help             Prints help."
        });
    }

    @Test
    public void shouldExecuteScriptFile() throws IOException {
        var cmd = new String[]{"java", "-jar", "target/toyscript.jar", "src/test/resources/script.toys"};
        var process = Runtime.getRuntime().exec(cmd);
        var in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        var lines = in.lines().map(String::trim).toArray(String[]::new);

        assertThat(lines).isEqualTo(new String[]{
                "Hello, World!",
                "This is message printed in my script file."
        });
    }

    @Test
    public void shouldStartInteractiveConsole() throws IOException {
        var cmd = new String[]{"java", "-jar", "target/toyscript.jar", "-i"};
        var process = Runtime.getRuntime().exec(cmd);
        var out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        var in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        out.write("/q");
        out.newLine();
        out.flush();
        var lines = in.lines().map(String::trim).toArray(String[]::new);

        assertThat(lines).isEqualTo(new String[]{
                "ToyScript 1.0-alpha",
                "Hint: type /q or /quit to exit the console",
                "ToyScript>"
        });
    }

    @Test
    public void shouldRunInteractiveSession() throws IOException, InterruptedException {
        var cmd = new String[]{"java", "-jar", "target/toyscript.jar", "-i"};
        var process = Runtime.getRuntime().exec(cmd);
        var out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        var in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        var queue = new LinkedBlockingQueue<String>();
        new Thread(() -> in.lines().forEach(line -> {
            try {
                queue.put(line);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        })).start();

        assertThat(queue.take()).isEqualTo("ToyScript 1.0-alpha");
        assertThat(queue.take()).isEqualTo("Hint: type /q or /quit to exit the console");

        out.write("var a = 1;");
        out.newLine();
        out.write("function f(a) { a++; return a + 1; }");
        out.newLine();
        out.write("a = f(a);");
        out.newLine();
        out.flush();
        assertThat(queue.take()).endsWith("3");

        out.write("b();");
        out.newLine();
        out.flush();
        assertThat(queue.take()).contains("Identifier b is not declared");

        out.write("/q");
        out.newLine();
        out.flush();
        assertThat(queue.take()).isEqualTo("ToyScript> ");
    }
}