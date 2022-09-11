package lang.toyscript.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class ToyScriptEngineTest {

    public ScriptEngine objectUnderTest;

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
    public void shouldTestRightAssociativity() throws ScriptException {
        // given
        var expression = "var a = 1; var b = 2; var c = 3 + b = b + a = 2 + 1;";

        // when
        objectUnderTest.put("z", 0);
        objectUnderTest.eval(expression);
        var a = objectUnderTest.get("a");
        var b = objectUnderTest.get("b");
        var c = objectUnderTest.get("c");

        // then
        assertThat(a).isEqualTo(3);
        assertThat(b).isEqualTo(5);
        assertThat(c).isEqualTo(8);
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
    public void shouldTestBasicLoops() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/basicLoops.toys");

        // when
        var result = objectUnderTest.eval(reader);
        var a = objectUnderTest.get("a");
        var b = objectUnderTest.get("b");
        var c = objectUnderTest.get("c");

        // then
        assertThat(result).isNull();
        assertThat(a).isEqualTo(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        assertThat(b).isEqualTo(List.of(0, "-", 2, 7, 4, 5, 6, 3, "-", 1));
        assertThat(c).isEqualTo(List.of("*", "x", "x", "*", "x", "x", "*"));
    }

    @Test
    public void shouldCallSimpleFunction() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/callSimpleFunction.toys");

        // when
        var result = objectUnderTest.eval(reader);
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
        var reader = resourceFileReader("/toys/multipleIfElse.toys");

        // when
        var result = objectUnderTest.eval(reader);
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
    public void shouldTestNestedStructures() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/nested.toys");

        // when
        objectUnderTest.eval(reader);

        // then
        assertThat(objectUnderTest.get("a")).isEqualTo(5);
        assertThat(objectUnderTest.get("b")).isEqualTo(3);
        assertThat(objectUnderTest.get("i")).isEqualTo(4);
    }

    @Test
    public void shouldRevertString() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/reverseString.toys");
        var original = "Some string to reverse.";

        // when
        objectUnderTest.eval(reader);
        var reversed = objectUnderTest.eval("reverseString(\"" + original + "\");");
        var reversedBack = objectUnderTest.eval("reverseString(\"" + reversed + "\");");

        // then
        assertThat(reversed).isEqualTo(".esrever ot gnirts emoS");
        assertThat(reversedBack).isEqualTo(original);
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

    @Test
    public void shouldRunBasicSortAlgorithms() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/basicSort.toys");

        // when
        objectUnderTest.eval(reader);
        objectUnderTest.eval("var a = array { 1, 5, 4, 8, 3, 1, 2, 5, 8, 9, 2, 7, 11, 6, 3, 2 };");
        objectUnderTest.eval("var b = array { 1, 5, 4, 8, 3, 1, 2, 5, 8, 9, 2, 7, 11, 6, 3, 2 };");
        objectUnderTest.eval("var c = array { 1, 5, 4, 8, 3, 1, 2, 5, 8, 9, 2, 7, 11, 6, 3, 2 };");
        objectUnderTest.eval("selectionSort(a);");
        objectUnderTest.eval("insertionSort(b);");
        objectUnderTest.eval("bubbleSort(c);");

        // then
        var sorted = List.of(1, 1, 2, 2, 2, 3, 3, 4, 5, 5, 6, 7, 8, 8, 9, 11);
        assertThat(objectUnderTest.get("a")).isEqualTo(sorted);
        assertThat(objectUnderTest.get("b")).isEqualTo(sorted);
        assertThat(objectUnderTest.get("c")).isEqualTo(sorted);
    }

    @Test
    public void shouldDoBinarySearch() throws ScriptException {
        // given
        var reader = resourceFileReader("/toys/binarySearch.toys");

        // when
        objectUnderTest.eval(reader);
        objectUnderTest.eval("var sorted = array { 1, 1, 2, 2, 2, 3, 3, 4, 5, 5, 7, 8, 8, 9 };");
        objectUnderTest.eval("var found = binarySearch(sorted, 4);");
        objectUnderTest.eval("var notFound = binarySearch(sorted, 6);");

        // then
        assertThat(objectUnderTest.get("found")).isEqualTo(7);
        assertThat(objectUnderTest.get("notFound")).isEqualTo(-11);
    }

    @Test
    public void shouldNotCatchSyntaxErrors() {
        // given
        var badSyntaxExpr = List.of(
                "var a() = 0;",
                "var a = 0",
                "b = 0",
                "struct { a = x }",
                "function () {}",
                "function x {}");

        // when
        var errors = badSyntaxExpr.stream()
                .map(expr -> catchThrowableOfType(
                        () -> objectUnderTest.eval("try { " + expr + " } catch {}"),
                        ScriptException.class))
                .map(ScriptException::getMessage)
                .toList();

        // then
        assertThat(errors).containsExactly(
                "mismatched input '(' expecting {';', '='} in script at line number 1 at column number 11",
                "missing ';' at '}' in script at line number 1 at column number 16",
                "missing ';' at '}' in script at line number 1 at column number 12",
                "missing ';' at '}' in script at line number 1 at column number 21",
                "missing ID at '(' in script at line number 1 at column number 15",
                "mismatched input '{' expecting '(' in script at line number 1 at column number 17");
    }

    @Test
    public void shouldCatchSemanticErrors() throws ScriptException {
        // given
        var badSemanticExpr = List.of(
                "var a = b;",
                "a = b;",
                "var a = 0; a();",
                "(struct { a = 1; }).b;",
                "function f() {}; var b = f;");

        // when
        objectUnderTest.eval("var i = 0; var errors = array[" + badSemanticExpr.size() + "];");
        for (var expr : badSemanticExpr) {
            objectUnderTest.eval("try { " + expr + " } catch (e) { errors[i++] = e; }");
        }

        // then
        assertThat(objectUnderTest.get("errors")).isEqualTo(List.of(
                "Identifier b is not declared",
                "Identifier a is not declared",
                "Expected function but was integer",
                "Member b not found",
                "Function reference cannot be used in expression"));
    }

    private static BufferedReader resourceFileReader(String path) {
        var stream = ToyScriptEngineTest.class.getResourceAsStream(path);
        assert stream != null;
        return new BufferedReader(new InputStreamReader(stream));
    }
}