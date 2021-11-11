package com.mattworzala.canary.internal.util.reflect;

import com.mattworzala.canary.internal.execution.TestExecutionListener;
import com.mattworzala.canary.internal.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.internal.util.safety.Env;
import com.mattworzala.canary.internal.util.safety.EnvType;

import java.lang.reflect.Method;

import static com.mattworzala.canary.internal.util.ClassLoaders.MINESTOM;
import static com.mattworzala.canary.internal.util.ClassLoaders.loadClassRequired;
import static org.junit.platform.commons.util.ReflectionUtils.*;

@Env(EnvType.PLATFORM)
public record ProxyTestCoordinator(Object testCoordinator) {
    private static final Class<?> testCoordinatorClass = loadClassRequired(MINESTOM, "com.mattworzala.canary.internal.execution.TestCoordinator");

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
