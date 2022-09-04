package lang.toyscript.engine;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.io.PrintWriter;
import java.io.Writer;

public class ParseErrorListener extends BaseErrorListener {

    private final PrintWriter err;

    public ParseErrorListener(Writer errorWriter) {
        this.err = errorWriter instanceof PrintWriter printWriter ? printWriter : new PrintWriter(errorWriter);
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg,
            RecognitionException e) {
        err.println("line " + line + ":" + charPositionInLine + " " + msg);
        throw e;
    }
}
