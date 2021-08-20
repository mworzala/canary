package com.mattworzala.canary.test.junit.discovery;

import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.test.junit.util.ClassLoaders;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.util.Optional;

/**
 * Creates a {@link CanaryTestDescriptor} from the given class, to be processed further in the future.
 * <p>
 * All test classes resolved will be loaded in {@link ClassLoaders#MINESTOM}
 */
public record ClassSelectorResolver(ClassFilter filter) implements SelectorResolver {

    @Override
    public Resolution resolve(ClassSelector selector, Context context) {
        return resolveTestClass(selector.getJavaClass(), context);
    }

    private Resolution resolveTestClass(Class<?> testClass, Context context) {
        if (!filter.test(testClass))
            return Resolution.unresolved();

        return context.addToParent(parent -> createTestDescriptor(parent, testClass))
                .map(Match::exact)
                .map(Resolution::match)
                .orElse(Resolution.unresolved());
    }

    private Optional<CanaryTestDescriptor> createTestDescriptor(TestDescriptor parent, Class<?> testClass) {
        // Create unique id with parent and child.
        UniqueId uniqueId = parent.getUniqueId().append("class", testClass.getSimpleName());

        // Reload the class in the Minestom classloader
        final var testClassReloaded = ClassLoaders.loadClass(ClassLoaders.MINESTOM, testClass);
        return Optional.ofNullable(testClassReloaded)
                .map(tc -> new CanaryTestDescriptor(uniqueId, tc));
    }
}
