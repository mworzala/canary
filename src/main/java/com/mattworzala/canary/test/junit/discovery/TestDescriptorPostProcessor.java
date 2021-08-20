package com.mattworzala.canary.test.junit.discovery;

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

public class TestDescriptorPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TestDescriptorPostProcessor.class);

    public boolean process(CanaryTestDescriptor test) {
        addChildrenRecursive(test);
        return !test.getChildren().isEmpty();
    }

    private void addChildrenRecursive(CanaryTestDescriptor parent) {
        try {
            TestSource source = parent.getSource().orElse(null);
            if (source == null) return; // Sanity check, should not happen

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
