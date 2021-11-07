package com.mattworzala.canary.platform.junit.discovery;

import com.mattworzala.canary.platform.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.platform.util.ClassLoaders;
import com.mattworzala.canary.platform.util.safety.EnvType;
import com.mattworzala.canary.platform.util.safety.Env;
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
@Env(EnvType.PLATFORM)
public record ClassSelectorResolver(ClassFilter filter) implements SelectorResolver {

    /**
     * Adds the class to the parent provided in `context`, with some resolution stuff to make junit happy.
     *
     * @see SelectorResolver#resolve(ClassSelector, Context)
     */
    @Override
    public Resolution resolve(ClassSelector selector, Context context) {
        if (!filter.test(selector.getJavaClass()))
            return Resolution.unresolved();

        return context.addToParent(parent -> createTestDescriptor(parent, selector.getJavaClass()))
                .map(Match::exact)
                .map(Resolution::match)
                .orElse(Resolution.unresolved());
    }

    /**
     * Create a {@link CanaryTestDescriptor} of the given class with the given parent after loading the class into the minestom classloader.
     * <p>
     * If the class could not be loaded into the minestom classloader, a console message should explain why.
     *
     * @param parent    The parent test descriptor
     * @param testClass The class to be reloaded into the minestom class loader
     * @return The test descriptor, or an empty optional if the class was not loadable in the minestom classloader.
     */
    private Optional<CanaryTestDescriptor> createTestDescriptor(TestDescriptor parent, Class<?> testClass) {
        // Create unique id with parent and child.
        UniqueId uniqueId = parent.getUniqueId().append("class", testClass.getSimpleName());

        // Reload the class in the Minestom classloader
        final var testClassReloaded = ClassLoaders.loadClass(ClassLoaders.MINESTOM, testClass);
        return Optional.ofNullable(testClassReloaded)
                .map(tc -> new CanaryTestDescriptor(uniqueId, tc));
    }
}
