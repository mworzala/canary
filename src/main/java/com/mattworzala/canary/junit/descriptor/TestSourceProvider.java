package com.mattworzala.canary.junit.descriptor;

import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.util.FunctionUtils;
import org.junit.platform.commons.util.LruCache;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestSourceProvider {
    private static final TestSource NULL_SOURCE = new TestSource() {
    };

    private final Map<TestDescription, TestSource> testSourceCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> methodsCache = Collections.synchronizedMap(new LruCache<>(31));

    public TestSource findTestSource(TestDescription description) {
        TestSource testSource = testSourceCache.computeIfAbsent(description, this::computeTestSource);
        return testSource == NULL_SOURCE ? null : testSource;
    }

    private TestSource computeTestSource(TestDescription description) {
        Class<?> testClass = description.getTestClass();
        if (testClass != null) {
            String methodName = TestDescription.getMethodName(description);
            if (methodName != null) {
                Method method = findMethod(testClass, sanitizeMethodName(methodName));
                if (method != null) {
                    return MethodSource.from(testClass, method);
                }
            }
            return ClassSource.from(testClass);
        }
        return NULL_SOURCE;
    }

    private String sanitizeMethodName(String methodName) {
        if (methodName.contains("[") && methodName.endsWith("]")) {
            // special case for parameterized tests
            return methodName.substring(0, methodName.indexOf("["));
        }
        return methodName;
    }

    private Method findMethod(Class<?> testClass, String methodName) {
        List<Method> methods = methodsCache.computeIfAbsent(testClass, clazz -> ReflectionUtils.findMethods(clazz, m -> true)).stream() //
                .filter(FunctionUtils.where(Method::getName, Predicate.isEqual(methodName))) //
                .collect(Collectors.toList());
        if (methods.isEmpty()) {
            return null;
        }
        if (methods.size() == 1) {
            return methods.get(0);
        }
        methods = methods.stream().filter(ModifierSupport::isPublic).collect(Collectors.toList());
        if (methods.size() == 1) {
            return methods.get(0);
        }
        return null;
    }
}
