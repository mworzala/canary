package com.mattworzala.canary.test.junit.discovery;

import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.util.Optional;

public class ClassSelectorResolver implements SelectorResolver {
    private static final Logger logger = LoggerFactory.getLogger(ClassSelectorResolver.class);

    private final ClassFilter filter;

    public ClassSelectorResolver(ClassFilter filter) {
        this.filter = filter;
    }

    @Override
    public Resolution resolve(ClassSelector selector, Context context) {
        return resolveTestClass(selector.getJavaClass(), context);
    }

    private Resolution resolveTestClass(Class<?> testClass, Context context) {
        if (!filter.test(testClass))
            return Resolution.unresolved();

//        logger.info(() -> "Found potential test class " + testClass.getName());
        return context.addToParent(parent -> Optional.of(createTestDescriptor(parent, testClass)))
                .map(Match::exact)
                .map(Resolution::match)
                .orElse(Resolution.unresolved());
    }

    private CanaryTestDescriptor createTestDescriptor(TestDescriptor parent, Class<?> testClass) {
        UniqueId uniqueId = parent.getUniqueId().append("class", testClass.getSimpleName());
        return new CanaryTestDescriptor(uniqueId, testClass);
    }
}
