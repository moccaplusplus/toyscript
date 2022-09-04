package lang.toyscript.engine.runtime.scope;

import org.antlr.v4.runtime.tree.TerminalNode;

public interface Register {

    void declare(TerminalNode id);

    Object read(TerminalNode id, int scope);

    default Object read(TerminalNode id) {
        return read(id, getDeclaringScope(id));
    }

    default void assign(TerminalNode id, Object value) {
        assign(id, value, getDeclaringScope(id));
    }

    void assign(TerminalNode id, Object value, int scope);

    int getDeclaringScope(TerminalNode id);

    int getCurrentScope();

    void enterScope();

    void exitScope();
}
