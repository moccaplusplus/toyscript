package lang.toyscript.engine.visitor;

import lang.toyscript.engine.error.SignalException;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Types {

    String VOID = "void";
    String BOOLEAN = "boolean";
    String INTEGER = "integer";
    String FLOAT = "float";
    String STRING = "string";
    String ARRAY = "array";
    String STRUCT = "struct";
    String FUNCTION = "function";
    String NATIVE = "native";

    static List<String> typeNames() {
        return List.of(VOID, BOOLEAN, INTEGER, FLOAT, STRING, ARRAY, STRUCT, FUNCTION, NATIVE);
    }

    static String typeName(Object obj) {
        return obj == null ? VOID : typeName(obj.getClass());
    }

    static String typeName(Class<?> type) {
        if (Boolean.class.isAssignableFrom(type)) return BOOLEAN;
        if (Integer.class.isAssignableFrom(type)) return INTEGER;
        if (Float.class.isAssignableFrom(type)) return FLOAT;
        if (String.class.isAssignableFrom(type)) return STRING;
        if (List.class.isAssignableFrom(type)) return ARRAY;
        if (Map.class.isAssignableFrom(type)) return STRUCT;
        if (Function.class.isAssignableFrom(type)) return FUNCTION;
        return NATIVE;
    }

    static String ensureStructKey(Map<?, ?> struct, TerminalNode id) {
        var key = id.getText();
        if (struct.containsKey(key)) return key;
        throw new SignalException.Throw(id.getSymbol(), "Member " + key + " not found");
    }

    static Object addExpr(Object o1, Object o2) {
        if (o1 instanceof String || o2 instanceof String) return String.valueOf(o1).concat(String.valueOf(o2));
        if (o1 instanceof Integer i1 && o2 instanceof Integer i2) return i1 + i2;
        return numberCast(o1).floatValue() + numberCast(o2).floatValue();
    }

    static Object subtractExpr(Object o1, Object o2) {
        if (o1 instanceof Integer i1 && o2 instanceof Integer i2) return i1 - i2;
        return numberCast(o1).floatValue() - numberCast(o2).floatValue();
    }

    static Number multiplyExpr(Object o1, Object o2) {
        if (o1 instanceof Integer i1 && o2 instanceof Integer i2) return i1 * i2;
        return numberCast(o1).floatValue() * numberCast(o2).floatValue();
    }

    static Number divideExpr(Object o1, Object o2) {
        if (o1 instanceof Integer i1 && o2 instanceof Integer i2) return i1 / i2;
        return numberCast(o1).floatValue() / numberCast(o2).floatValue();
    }

    static Integer moduloExpr(Object o1, Object o2) {
        return numberCast(o1).intValue() % numberCast(o2).intValue();
    }

    static Boolean equalsExpr(Object o1, Object o2) {
        if (o1 == null) return o2 == null;
        if (o1 == o2) return true;
        if (o1 instanceof Number n1 && o2 instanceof Number n2) {
            if (n1 instanceof Integer && n2 instanceof Integer) return n1.intValue() == n2.intValue();
            return n1.floatValue() == n2.floatValue();
        }
        return o1.equals(o2);
    }

    static Boolean lessThenExpr(Object o1, Object o2) {
        if (o1 instanceof Integer i1 && o2 instanceof Integer i2) return i1 < i2;
        return numberCast(o1).floatValue() < numberCast(o2).floatValue();
    }

    static boolean greaterThenExpr(Object o1, Object o2) {
        if (o1 instanceof Integer i1 && o2 instanceof Integer i2) return i1 > i2;
        return numberCast(o1).floatValue() > numberCast(o2).floatValue();
    }

    static Object unaryMinExpr(Object obj) {
        var num = numberCast(obj);
        if (num instanceof Integer) return -num.intValue();
        return -num.floatValue();
    }

    static Number numberCast(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Boolean b) return b ? 1 : 0;
        if (obj instanceof Integer i) return i;
        if (obj instanceof Float f) return f;
        if (obj instanceof String s) return s.isBlank() ? 0 : 1;
        return 1;
    }

    static Boolean boolCast(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean b) return b;
        if (obj instanceof Integer i) return i != 0;
        if (obj instanceof Float f) return f != 0;
        if (obj instanceof String s) return !s.isBlank();
        return true;
    }

    static String ellipsize(Object o) {
        var s = String.valueOf(o);
        return s.length() > 32 ? s.substring(0, 30).trim() + "..." : s;
    }

    static String errorMsg(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getClass().getSimpleName() + " " + cause.getMessage();
    }
}
