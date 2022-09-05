package lang.toyscript.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.util.List;

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

    @Test
    public void shouldTestBasicArrayExpressions() throws ScriptException {
        // given
        var expression = String.join("\n",
                "var x = array { 1, \"Two\", 3 };",
                "var y = array[3];",
                "var i = 0;",
                "while(i < 3) { y[i] = x[i]; i++; }",
                "var z = y[0] - x[2] * -2;");

        // when
        objectUnderTest.eval(expression);
        var engineScope = objectUnderTest.getBindings(ScriptContext.ENGINE_SCOPE);
        @SuppressWarnings("unchecked") var x = (List<Object>) engineScope.get("x");
        @SuppressWarnings("unchecked") var y = (List<Object>) engineScope.get("y");
        var z = (Number) engineScope.get("z");

        // then
        assertThat(x).containsExactly(1, "Two", 3);
        assertThat(y).isEqualTo(x);
        assertThat(z).isEqualTo(7);
    }

    @Test
    public void shouldTestBasicStructExpressions() throws ScriptException {
        // given
        var expression = "var x = struct { a = 1; b = struct { c = 2; }; }; var y = struct { value = (x.a + x.b.c); }; y.value;";

        // when
        var result = objectUnderTest.eval(expression);

        //then
        assertThat(result).isEqualTo(3);
    }
}