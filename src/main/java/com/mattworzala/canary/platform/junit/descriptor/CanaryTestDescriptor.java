package com.mattworzala.canary.platform.junit.descriptor;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

@Environment(EnvType.GLOBAL)
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
        return Type.TEST; // Any other option does not show in IntelliJ test window correctly.
    }


    @NotNull
    public Collection<TestDescriptor> getChildrenMutable() {
        return children;
    }

    @Nullable
    public Path getStructureLocation() {
        TestSource source = getSource().orElse(null);
        if (source instanceof MethodSource methodSource) {
            Path base = Paths.get("../src/test/resources");
            String location = methodSource.getJavaClass().getName().replace('$', '/').replace('.', '/');
            String name = methodSource.getJavaMethod().getName() + ".json";
            return base.resolve(location).resolve(name);
        }
        return null;
    }

}
