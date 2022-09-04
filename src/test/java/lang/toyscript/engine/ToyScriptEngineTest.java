package lang.toyscript.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

public class ToyScriptEngineTest {

    public ToyScriptEngine objectUnderTest;

    @BeforeEach
    public void beforeEach() {
        objectUnderTest = new ToyScriptEngine();
    }

    @Test
    public void shouldEvalSimpleScriptLeavingEmptyStack() throws ScriptException {
        // given
        var engineScope = objectUnderTest.getBindings(ScriptContext.ENGINE_SCOPE);
        var expression = "var x = 123; x = 4 + x * 2;";

        // when
        var result = objectUnderTest.eval(expression);

        // then
        assertThat(result).isNull();
        assertThat(engineScope).containsKey("x");
        assertThat(engineScope.get("x")).isEqualTo(250);
        assertThat(engineScope).doesNotContainKey("y");
        assertThat(engineScope).hasSize(1);
    }

    @Test
    public void shouldEvalSimpleScriptLeavingValueOnStack() throws ScriptException {
        // given
        var engineScope = objectUnderTest.getBindings(ScriptContext.ENGINE_SCOPE);
        var expression = "var x = 123; x = 4 + x * 2 + 5; x;";

        // when
        var result = objectUnderTest.eval(expression);

        // then
        assertThat(result).isNotNull().isEqualTo(255);
        assertThat(engineScope).containsKey("x");
        assertThat(engineScope).doesNotContainKey("y");
        assertThat(engineScope.get("x")).isEqualTo(255);
        assertThat(engineScope).hasSize(1);
    }

    @Test
    public void shouldEvalIncrementAndDecrement() throws ScriptException {
        // given
        var engineScope = objectUnderTest.getBindings(ScriptContext.ENGINE_SCOPE);
        var expression = "var x = 20; var y = 10; while (x > 10) { x--; y++; z--; };";
        engineScope.put("z", 0);

        // when
        objectUnderTest.eval(expression);
        var x = engineScope.get("x");
        var y = engineScope.get("y");
        var z = engineScope.get("z");

        // then
        assertThat(x).isEqualTo(10);
        assertThat(y).isEqualTo(20);
        assertThat(z).isEqualTo(-10);
    }
}