package com.mattworzala.canary.platform.reflect;

import com.mattworzala.canary.platform.givemeahome.TestExecutionListener;
import com.mattworzala.canary.platform.junit.descriptor.CanaryEngineDescriptor;

import java.lang.reflect.Method;

import static com.mattworzala.canary.platform.util.ClassLoaders.MINESTOM;
import static com.mattworzala.canary.platform.util.ClassLoaders.loadClassRequired;
import static org.junit.platform.commons.util.ReflectionUtils.*;

public record ProxyTestCoordinator(Object testCoordinator) {
    private static final Class<?> testCoordinatorClass = loadClassRequired(MINESTOM, "com.mattworzala.canary.server.execution.TestCoordinator");

    // @formatter:off
    private static final Method indexTests = getRequiredMethod(testCoordinatorClass, "indexTests", CanaryEngineDescriptor.class);
    public void indexTests(CanaryEngineDescriptor rootDescriptor) {
        invokeMethod(indexTests, testCoordinator, rootDescriptor);
    }
    // @formatter:on

    // @formatter:off
    private static final Method execute = getRequiredMethod(testCoordinatorClass, "execute", TestExecutionListener.class);
    public void execute(TestExecutionListener listener) {
        invokeMethod(execute, testCoordinator, listener);
    }
    // @formatter:on
}
