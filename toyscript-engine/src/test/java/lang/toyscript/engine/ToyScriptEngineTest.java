package lang.toyscript.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        var expression = "var x = 123; { x = 4 + x * 2; }";

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
        var expression = "var x = 123; x = 4 + x * 2 + 5;";

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
        var reader = resourceFileReader("/toys/basicArrayExpressions.toys");

        // when
        objectUnderTest.eval(reader);
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
        var reader = resourceFileReader("/toys/basicStructExpressions.toys");

        // when
        var result = objectUnderTest.eval(reader);

        //then
        assertThat(result).isEqualTo(3);
    }

    @Test
    public void shouldCallSimpleFunction() throws ScriptException {
        // given
        var expression = resourceFileReader("/toys/callSimpleFunction.toys");
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
        var expression = resourceFileReader("/toys/multipleIfElse.toys");
        // when
        var result = objectUnderTest.eval(expression);
        var a = (Number) objectUnderTest.get("a");
        var b = (Number) objectUnderTest.get("b");
        var c = (Number) objectUnderTest.get("c");
        var x = (Number) objectUnderTest.get("x");

        // then
        assertThat(result).isNotNull().isEqualTo(6);
        assertThat(a).isEqualTo(7);
        assertThat(b).isEqualTo(2);
        assertThat(c).isEqualTo(null);
        assertThat(x).isEqualTo(7);
    }

    @Test
    public void shouldHandleSimpleTryCatch() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/simpleTryCatch.toys");

        // when
        objectUnderTest.eval(reader);

        // then
        assertThat(objectUnderTest.get("x")).isEqualTo(4);
        assertThat(objectUnderTest.get("y")).isEqualTo(1);
        assertThat(objectUnderTest.get("z")).isEqualTo(5);
        assertThat(objectUnderTest.get("caught")).isEqualTo("error-2");
    }

    @Test
    public void shouldTestMultipleScopes() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/multipleScopes.toys");

        // when
        objectUnderTest.eval(reader);

        // then
        assertThat(objectUnderTest.get("a")).isEqualTo(1);
        assertThat(objectUnderTest.get("b")).isEqualTo(6);
        assertThat(objectUnderTest.get("c")).isEqualTo(40);
    }

    @Test
    public void shouldTestRecursion() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/recursion.toys");

        // when
        objectUnderTest.eval(reader);

        // then
        assertThat(objectUnderTest.get("f5")).isEqualTo(120);
        assertThat(objectUnderTest.get("f10")).isEqualTo(3628800);
    }

    @Test
    public void shouldComputeFibonacciSeq() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/fibonacci.toys");

        // when
        objectUnderTest.eval(reader);

        // then
        assertThat(objectUnderTest.get("f1")).isEqualTo(13);
        assertThat(objectUnderTest.get("f2")).isEqualTo(13);
        assertThat(objectUnderTest.get("f3")).isEqualTo(13);
        assertThat(objectUnderTest.get("f3_37")).isEqualTo(24157817);
    }

    private static BufferedReader resourceFileReader(String path) {
        var stream = ToyScriptEngineTest.class.getResourceAsStream(path);
        assert stream != null;
        return new BufferedReader(new InputStreamReader(stream));
    }
}