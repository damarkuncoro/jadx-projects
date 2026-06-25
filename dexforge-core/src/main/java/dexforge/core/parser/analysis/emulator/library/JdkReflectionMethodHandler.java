package dexforge.core.parser.analysis.emulator.library;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.*;

/**
 * REUSEABLE: Generic, SOLID reflection-based method handler for JDK classes.
 * Dynamically maps Dalvik signatures to JVM types and executes them.
 */
public final class JdkReflectionMethodHandler implements VirtualMethodHandler {
    private static final Set<String> ALLOWED_PREFIXES = new HashSet<>(Arrays.asList(
        "Ljava/lang/String;",
        "Ljava/lang/StringBuilder;",
        "Ljava/lang/StringBuffer;",
        "Ljava/lang/Math;",
        "Ljava/lang/Integer;",
        "Ljava/lang/Long;",
        "Ljava/lang/Double;",
        "Ljava/lang/Float;",
        "Ljava/lang/Boolean;",
        "Ljava/lang/Byte;",
        "Ljava/lang/Character;",
        "Ljava/lang/Short;",
        "Ljava/lang/System;",
        "Ljava/lang/Class;",
        "Ljava/lang/reflect/Method;",
        "Ljava/lang/reflect/Constructor;",
        "Ljava/lang/reflect/Field;",
        "Ljava/lang/reflect/Array;",
        "Ljava/util/Arrays;",
        "Ljava/util/HashMap;",
        "Ljava/util/ArrayList;",
        "Ljava/util/Map;",
        "Ljava/util/List;",
        "Ljava/util/Iterator;"
    ));

    @Override
    public boolean canHandle(String methodSignature) {
        return ALLOWED_PREFIXES.stream().anyMatch(methodSignature::startsWith);
    }

    @Override
    public Object execute(String signature, List<Object> args) throws Exception {
        int arrowIdx = signature.indexOf("->");
        if (arrowIdx == -1) return null;

        String classSig = signature.substring(0, arrowIdx);
        String methodSig = signature.substring(arrowIdx + 2);

        int parenIdx = methodSig.indexOf('(');
        if (parenIdx == -1) return null;

        String methodName = methodSig.substring(0, parenIdx);
        String paramsSig = methodSig.substring(parenIdx);

        // Convert Dalvik class signature Ljava/lang/String; to java.lang.String
        String className = classSig.substring(1, classSig.length() - 1).replace('/', '.');
        Class<?> clazz = Class.forName(className);

        // Parse parameters to resolve method signature
        List<Class<?>> paramClasses = parseParamTypes(paramsSig);
        Class<?>[] paramArray = paramClasses.toArray(new Class<?>[0]);

        // Handle constructor initialization
        if (methodName.equals("<init>")) {
            java.lang.reflect.Constructor<?> constructor = clazz.getDeclaredConstructor(paramArray);
            constructor.setAccessible(true);

            // First argument of constructor call in registers is the placeholder receiver instance
            List<Object> constructorArgsList = args.subList(1, args.size());
            Object[] constructorArgs = constructorArgsList.toArray();
            return constructor.newInstance(constructorArgs);
        }

        // Find the method. We check for exact match and then assignable match.
        Method method = getCompatibleMethod(clazz, methodName, paramArray);
        if (method == null) {
            throw new NoSuchMethodException("Method not found: " + signature);
        }

        method.setAccessible(true);

        if (Modifier.isStatic(method.getModifiers())) {
            Object[] invokeArgs = prepareArgs(method.getParameterTypes(), args);
            return method.invoke(null, invokeArgs);
        } else {
            if (args.isEmpty() || args.get(0) == null) {
                throw new NullPointerException("Receiver instance of method is null: " + signature);
            }
            Object receiver = args.get(0);
            List<Object> methodArgs = args.subList(1, args.size());
            Object[] invokeArgs = prepareArgs(method.getParameterTypes(), methodArgs);
            return method.invoke(receiver, invokeArgs);
        }
    }

    private Object[] prepareArgs(Class<?>[] paramTypes, List<Object> args) {
        Object[] result = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (i < args.size()) {
                result[i] = convertArg(args.get(i), paramTypes[i]);
            }
        }
        return result;
    }

    private Object convertArg(Object arg, Class<?> targetType) {
        if (arg == null) return null;
        if (targetType.isInstance(arg)) return arg;

        // Convert Object[] to byte[] if needed (crucial for emulator-jvm bridge)
        if (arg instanceof Object[] && targetType == byte[].class) {
            Object[] arr = (Object[]) arg;
            byte[] bytes = new byte[arr.length];
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] instanceof Number) bytes[i] = ((Number) arr[i]).byteValue();
            }
            return bytes;
        }

        // Handle primitive widening/narrowing
        if (arg instanceof Number) {
            Number n = (Number) arg;
            if (targetType == int.class || targetType == Integer.class) return n.intValue();
            if (targetType == long.class || targetType == Long.class) return n.longValue();
            if (targetType == short.class || targetType == Short.class) return n.shortValue();
            if (targetType == byte.class || targetType == Byte.class) return n.byteValue();
        }

        return arg;
    }

    private List<Class<?>> parseParamTypes(String sig) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        int i = 0;
        if (sig.charAt(i) != '(') return classes;
        i++;
        while (i < sig.length() && sig.charAt(i) != ')') {
            int arrayDepth = 0;
            while (sig.charAt(i) == '[') {
                arrayDepth++;
                i++;
            }
            char c = sig.charAt(i);
            Class<?> baseClass;
            if (c == 'L') {
                int semi = sig.indexOf(';', i);
                String className = sig.substring(i + 1, semi).replace('/', '.');
                baseClass = Class.forName(className);
                i = semi + 1;
            } else {
                baseClass = getPrimitiveClass(c);
                i++;
            }
            if (arrayDepth > 0) {
                baseClass = getArrayClass(baseClass, arrayDepth);
            }
            classes.add(baseClass);
        }
        return classes;
    }

    private Class<?> getPrimitiveClass(char c) {
        switch (c) {
            case 'V': return void.class;
            case 'Z': return boolean.class;
            case 'B': return byte.class;
            case 'S': return short.class;
            case 'C': return char.class;
            case 'I': return int.class;
            case 'J': return long.class;
            case 'F': return float.class;
            case 'D': return double.class;
            default: throw new IllegalArgumentException("Unknown primitive type char: " + c);
        }
    }

    private Class<?> getArrayClass(Class<?> baseClass, int depth) throws ClassNotFoundException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append('[');
        }
        if (baseClass.isPrimitive()) {
            char code = getPrimitiveCode(baseClass);
            sb.append(code);
        } else {
            sb.append('L').append(baseClass.getName()).append(';');
        }
        return Class.forName(sb.toString());
    }

    private char getPrimitiveCode(Class<?> primitiveClass) {
        if (primitiveClass == void.class) return 'V';
        if (primitiveClass == boolean.class) return 'Z';
        if (primitiveClass == byte.class) return 'B';
        if (primitiveClass == short.class) return 'S';
        if (primitiveClass == char.class) return 'C';
        if (primitiveClass == int.class) return 'I';
        if (primitiveClass == long.class) return 'J';
        if (primitiveClass == float.class) return 'F';
        if (primitiveClass == double.class) return 'D';
        throw new IllegalArgumentException("Not a primitive: " + primitiveClass);
    }

    private Method getCompatibleMethod(Class<?> clazz, String name, Class<?>[] params) {
        try {
            return clazz.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == params.length) {
                    Class<?>[] mParams = m.getParameterTypes();
                    boolean match = true;
                    for (int i = 0; i < params.length; i++) {
                        if (!mParams[i].isAssignableFrom(params[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) return m;
                }
            }
            if (clazz.getSuperclass() != null) {
                return getCompatibleMethod(clazz.getSuperclass(), name, params);
            }
            return null;
        }
    }
}
