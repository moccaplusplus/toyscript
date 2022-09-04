package lang.toyscript.engine.runtime.scope;

public interface ScopeManager {

    void declare(String identifier);

    Object getValue(String identifier);

    default void setValue(String identifier, Object value) {
        setValue(identifier, value, getDeclaringScope(identifier));
    }

    void setValue(String identifier, Object value, int scope);

    int getDeclaringScope(String identifier);

    int getCurrentScope();

    void enterScope();

    void exitScope();
}
