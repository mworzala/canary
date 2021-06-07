package com.mattworzala.canary.test.junit.descriptor;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.lang.reflect.Method;

public class JupiterCanaryTestDescriptor extends AbstractTestDescriptor {
    private final Class<?> testClass;
    private final Method testMethod;


    public JupiterCanaryTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
        super(uniqueId, uniqueId.getLastSegment().getValue());
        this.testClass = testClass;
        this.testMethod = null;
    }

    public JupiterCanaryTestDescriptor(UniqueId uniqueId, Method testMethod) {
        super(uniqueId, uniqueId.getLastSegment().getValue());
        this.testClass = testMethod.getDeclaringClass();
        this.testMethod = testMethod;
    }

    @Override
    public Type getType() {
        return Type.TEST;
//        return this.testMethod == null ? Type.CONTAINER : Type.TEST;
//        return this.testMethod == null ? Type.CONTAINER_AND_TEST : Type.TEST;
    }

    public Class<?> getTestClass() {
        return testClass;
    }
}
