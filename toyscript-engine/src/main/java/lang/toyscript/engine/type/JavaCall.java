package lang.toyscript.engine.type;

import java.util.function.Function;

public class JavaCall implements Function<Object[], Object> {

    @FunctionalInterface
    public interface Delegate {
        Object apply(Object[] args) throws Exception;
    }

    private final String strVal;
    private final Delegate delegate;

    public JavaCall(String strVal, Delegate delegate) {
        this.strVal = strVal;
        this.delegate = delegate;
    }

    @Override
    public Object apply(Object[] args) {
        try {
            return delegate.apply(args);
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
