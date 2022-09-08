package lang.toyscript.console;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class ToyScriptConsoleTest {

    @Test
    public void shouldPrintHelp() throws IOException {
        // given
        var cmd = new String[]{"java", "-jar", "target/toyscript.jar"};
        var process = Runtime.getRuntime().exec(cmd);
        var in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // when
        var lines = in.lines().map(String::trim).toArray(String[]::new);

        // then
        assertThat(lines).isEqualTo(new String[]{
                "Usage: toyscript [-i] [-h] /path/to/script.toys [-e utf-8]",
                "Options:",
                "-e, --encoding         Optional. Script file encoding. Default: utf-8.",
                "-i, --interactive      Interactive mode. Path to file not needed.",
                "-h, --help             Prints help."
        });
    }

    @Test
    public void shouldStartInteractiveConsole() throws IOException {
        // given
        var cmd = new String[]{"java", "-jar", "target/toyscript.jar", "-i"};
        var process = Runtime.getRuntime().exec(cmd);
        var out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        var in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // when
        out.write("/q");
        out.newLine();
        out.flush();
        var lines = in.lines().map(String::trim).toArray(String[]::new);

        // then
        assertThat(lines).isEqualTo(new String[]{
                "ToyScript 1.0-alpha",
                "Hint: type /q or /quit to exit the console",
                "ToyScript>"
        });
    }
}