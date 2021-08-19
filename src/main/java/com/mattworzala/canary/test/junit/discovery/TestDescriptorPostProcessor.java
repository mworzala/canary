package com.mattworzala.canary.test.junit.discovery;

import com.mattworzala.canary.test.InWorldTest;
import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
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
        try {
            TestSource source = parent.getSource().orElse(null);
            if (source == null) {
                return;
            }
            if (source instanceof ClassSource classSource) {
                var testClass = classSource.getJavaClass();
                for (Method method : testClass.getMethods()) {
                    Annotation testAnnotation = method.getAnnotation((Class<? extends Annotation>) Class.forName("com.mattworzala.canary.test.InWorldTest", true, MinestomRootClassLoader.getInstance()));
                    if (testAnnotation == null) continue;

                    String methodId = String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
                    UniqueId uniqueId = parent.getUniqueId().append("test", methodId);
                    var child = new CanaryTestDescriptor(uniqueId, method);
                    parent.addChild(child);
                }
            }
        } catch (Exception ignored) {}


    }
}
