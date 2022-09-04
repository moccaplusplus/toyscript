package lang.toyscript.engine.runtime;

public final class TypeUtils {

    public static Number numberAdd(Number num0, Number num1) {
        if (num0 instanceof Integer && num1 instanceof Integer) return num0.intValue() + num1.intValue();
        return num0.floatValue() + num1.floatValue();
    }

    public static Number numberSub(Number num0, Number num1) {
        if (num0 instanceof Integer && num1 instanceof Integer) return num0.intValue() - num1.intValue();
        return num0.floatValue() - num1.floatValue();
    }

    public static Number numberMul(Number num0, Number num1) {
        if (num0 instanceof Integer && num1 instanceof Integer) return num0.intValue() * num1.intValue();
        return num0.floatValue() * num1.floatValue();
    }

    public static Number numberDiv(Number num0, Number num1) {
        if (num0 instanceof Integer && num1 instanceof Integer) return num0.intValue() / num1.intValue();
        return num0.floatValue() / num1.floatValue();
    }

    public static Integer numberMod(Number num0, Number num1) {
        return num0.intValue() % num1.intValue();
    }

    public static Boolean numberEquals(Number num0, Number num1) {
        if (num0 instanceof Integer && num1 instanceof Integer) return num0.intValue() == num1.intValue();
        return num0.floatValue() == num1.floatValue();
    }

    public static Boolean numberLessThen(Number num0, Number num1) {
        if (num0 instanceof Integer && num1 instanceof Integer) return num0.intValue() < num1.intValue();
        return num0.floatValue() < num1.floatValue();
    }

    public static boolean numberGreaterThen(Number num0, Number num1) {
        if (num0 instanceof Integer && num1 instanceof Integer) return num0.intValue() > num1.intValue();
        return num0.floatValue() > num1.floatValue();
    }

    public static Number unaryMin(Number obj) {
        if (obj instanceof Integer) return -obj.intValue();
        return -obj.floatValue();
    }

    public static Number numberCast(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Boolean b) return b ? 1 : 0;
        if (obj instanceof Integer i) return i;
        if (obj instanceof Float f) return f;
        if (obj instanceof String s) return s.isBlank() ? 0 : 1;
        return 1;
    }

    public static Boolean boolCast(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean b) return b;
        if (obj instanceof Integer i) return i != 0;
        if (obj instanceof Float f) return f != 0;
        if (obj instanceof String s) return !s.isBlank();
        return true;
    }
}
