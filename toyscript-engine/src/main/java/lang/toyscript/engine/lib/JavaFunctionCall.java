package lang.toyscript.engine.lib;

import java.util.function.Function;

public class JavaFunctionCall implements Function<Object[], Object> {

    @FunctionalInterface
    public interface Delegate {
        Object apply(Object[] args) throws Exception;
    }

    private final String strVal;

    private final Delegate delegate;

    private final int length;

    public JavaFunctionCall(Delegate delegate) {
        this(new String[0], delegate);
    }

    public JavaFunctionCall(String param, Delegate delegate) {
        this(new String[]{param}, delegate);
    }

    public JavaFunctionCall(String[] params, Delegate delegate) {
        this.delegate = delegate;
        strVal = "function(" + String.join(", ", params) + ")";
        length = params.length;
    }

    @Override
    public Object apply(Object[] args) {
        try {
            if (args.length >= length) return delegate.apply(args);
            var resized = new Object[length];
            System.arraycopy(args, 0, resized, 0, args.length);
            return delegate.apply(resized);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return strVal;
    }
}
