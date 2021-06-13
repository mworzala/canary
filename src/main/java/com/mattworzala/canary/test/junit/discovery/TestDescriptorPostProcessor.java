package com.mattworzala.canary.test.junit.discovery;

import com.mattworzala.canary.test.InWorldTest;
import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.UniqueId;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public class TestDescriptorPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TestDescriptorPostProcessor.class);

    private final Predicate<Class<?>> isPotentialTestClass;

    public TestDescriptorPostProcessor(Predicate<Class<?>> isPotentialTestClass) {
        this.isPotentialTestClass = isPotentialTestClass;
    }

    public void process(CanaryTestDescriptor test) {
        addChildrenRecursive(test);
    }

    private void addChildrenRecursive(CanaryTestDescriptor parent) {
        Class<?> testClass = parent.getTestClass();
        for (Method method : testClass.getMethods()) {
            InWorldTest testAnnotation = method.getAnnotation(InWorldTest.class);
            if (testAnnotation == null) continue;

//            logger.info(() -> "Found test method " + method.getName());
            String methodId = String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
            UniqueId uniqueId = parent.getUniqueId().append("test", methodId);
            var child = new CanaryTestDescriptor(uniqueId, method);
            parent.addChild(child);
        }
    }
}
