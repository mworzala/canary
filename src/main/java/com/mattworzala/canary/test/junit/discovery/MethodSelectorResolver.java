package com.mattworzala.canary.test.junit.discovery;

import com.mattworzala.canary.test.junit.InWorldTest;
import com.mattworzala.canary.test.junit.descriptor.JupiterCanaryTestDescriptor;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.lang.reflect.Method;
import java.util.Optional;

public class MethodSelectorResolver implements SelectorResolver {
    @Override
    public Resolution resolve(MethodSelector selector, Context context) {
        Method method = selector.getJavaMethod();
        InWorldTest annotation = method.getAnnotation(InWorldTest.class);
        if (annotation == null) return Resolution.unresolved();

        return context.addToParent(parent -> {
            String methodId = String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
            UniqueId uniqueId = parent.getUniqueId().append("runner", methodId);
            return Optional.of(new JupiterCanaryTestDescriptor(uniqueId, (Class<?>) null /*todo */));
        })
                .map(Match::exact)
                .map(Resolution::match)
                .orElse(Resolution.unresolved());
    }

}
