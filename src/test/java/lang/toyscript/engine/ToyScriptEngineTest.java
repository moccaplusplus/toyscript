package lang.toyscript.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.util.List;

import static java.lang.String.join;
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
        var expression = "var x = 123; x = 4 + x * 2 + 5; return x;";

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
        var expression = "var x = 20; var y = 10; while (x > 10) { x--; y++; z--; };";

        // when
        objectUnderTest.put("z", 0);
        objectUnderTest.eval(expression);
        var x = objectUnderTest.get("x");
        var y = objectUnderTest.get("y");
        var z = objectUnderTest.get("z");

        // then
        assertThat(x).isEqualTo(10);
        assertThat(y).isEqualTo(20);
        assertThat(z).isEqualTo(-10);
    }

    @Test
    public void shouldTestBasicArrayExpressions() throws ScriptException {
        // given
        var expression = join("\n",
                "var x = array { 1, \"Two\", 3 };",
                "var y = array[3];",
                "var i = 0;",
                "while(i < 3) { y[i] = x[i]; i++; }",
                "var z = y[0] - x[2] * -2;");

        // when
        objectUnderTest.eval(expression);
        @SuppressWarnings("unchecked") var x = (List<Object>) objectUnderTest.get("x");
        @SuppressWarnings("unchecked") var y = (List<Object>) objectUnderTest.get("y");
        var z = (Number) objectUnderTest.get("z");

        // then
        assertThat(x).containsExactly(1, "Two", 3);
        assertThat(y).isEqualTo(x);
        assertThat(z).isEqualTo(7);
    }

    @Test
    public void shouldTestBasicStructExpressions() throws ScriptException {
        // given
        var expression = join("\n",
                "var x = struct { a = 1; b = struct { c = 2; }; };",
                "var y = struct { value = (x.a + x.b.c); };",
                "return y.value;");

        // when
        var result = objectUnderTest.eval(expression);

        //then
        assertThat(result).isEqualTo(3);
    }

    @Test
    public void shouldCallSimpleFunction() throws ScriptException {
        // given
        var expression = join("\n",
                "function fun (a, b) { var c = 100; var x = 2 * (a + b); return c - x; }",
                "var a = 1; var b = 2; var c = 3;",
                "var r = fun(c, b, a);",
                "return r;");
        // when
        var result = objectUnderTest.eval(expression);
        var a = (Number) objectUnderTest.get("a");
        var b = (Number) objectUnderTest.get("b");
        var c = (Number) objectUnderTest.get("c");
        var r = (Number) objectUnderTest.get("r");
        var x = objectUnderTest.get("x");

        //then
        assertThat(result).isEqualTo(90);
        assertThat(r).isEqualTo(result);
        assertThat(a).isEqualTo(1);
        assertThat(b).isEqualTo(2);
        assertThat(c).isEqualTo(3);
        assertThat(x).isNull();
    }

    @Test
    public void shouldTestMultipleIfElse() throws ScriptException {
        var expression = join("\n",
                "var a; var b; var c; var x = 5;",
                "if (a == 1) { b = 1; } else if (a == null) b = 2; else c = 3;",
                "if (b == 1) { c = 2; } else if (b == null) c = 1; else a = 7;",
                "if (a == 7) { if (c == 2 || b == 2) x++; }; x++;"
        );
        // when
        var result = objectUnderTest.eval(expression);
        var a = (Number) objectUnderTest.get("a");
        var b = (Number) objectUnderTest.get("b");
        var c = (Number) objectUnderTest.get("c");
        var x = (Number) objectUnderTest.get("x");

        // then
        assertThat(a).isEqualTo(7);
        assertThat(b).isEqualTo(2);
        assertThat(c).isEqualTo(null);
        assertThat(x).isEqualTo(7);
    }
}