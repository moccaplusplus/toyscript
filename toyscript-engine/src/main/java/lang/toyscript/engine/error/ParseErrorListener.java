package lang.toyscript.engine.error;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ParseErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg,
            RecognitionException e) {
        throw new ParseException(msg, line, charPositionInLine);
    }
}
