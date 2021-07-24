package com.mattworzala.canary.test.junit.descriptor;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;

public class CanaryTestDescriptor extends AbstractTestDescriptor {

    public CanaryTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
        super(uniqueId, uniqueId.getLastSegment().getValue(),
                ClassSource.from(testClass));
    }

    public CanaryTestDescriptor(UniqueId uniqueId, Method testMethod) {
        super(uniqueId, uniqueId.getLastSegment().getValue(),
                MethodSource.from(testMethod.getDeclaringClass(), testMethod));
    }

    @Override
    public Type getType() {
        return Type.TEST;
//        return this.testMethod == null ? Type.CONTAINER : Type.TEST;
//        return this.testMethod == null ? Type.CONTAINER_AND_TEST : Type.TEST;
    }
}
