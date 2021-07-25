package com.mattworzala.canary.test.sandbox;

import com.mattworzala.canary.test.junit.CanaryTestEngine;
import com.mattworzala.canary.test.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class SandboxTestEnvironment {
    public record DiscoverySummary(int packages, int files, int tests) {}

    private static SandboxTestEnvironment instance;
    public static SandboxTestEnvironment getInstance() {
        if (instance == null)
            instance = new SandboxTestEnvironment();
        return instance;
    }

    private final CanaryTestEngine engine;
    private CanaryEngineDescriptor root;

    // Indexed data
    private final Set<String> testPackages = new HashSet<>();
    private final Set<String> testFiles = new HashSet<>();
    private final Set<String> uniqueTests = new HashSet<>();

    private SandboxTestEnvironment() {
        engine = new CanaryTestEngine();
        discover();
    }

    public DiscoverySummary discover() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage(""))
                .build();
        root = (CanaryEngineDescriptor) engine.discover(request, UniqueId.forEngine(engine.getId()));
        return indexTests();
    }

    public @NotNull CanaryEngineDescriptor getRoot() {
        assert root != null;
        return root;
    }

    public Stream<String> getTestPackages() {
        return testPackages.stream();
    }

    public Stream<String> getTestFiles() {
        return testFiles.stream();
    }

    public Stream<String> getUniqueTests() {
        return uniqueTests.stream();
    }

    private DiscoverySummary indexTests() {
        testPackages.clear();
        testFiles.clear();
        uniqueTests.clear();

        indexTestRecursive(root);

        return new DiscoverySummary(testPackages.size(), testFiles.size(), uniqueTests.size());
    }

    private void indexTestRecursive(TestDescriptor test) {
        TestSource source = test.getSource().orElse(null);

        // Index this test
        if (source != null) {
            if (source instanceof ClassSource classSource) {
                var testClass = classSource.getJavaClass();
                testPackages.add(testClass.getPackageName());
                testFiles.add(classSource.getClassName()); //todo is canonical name valid in all cases?
            } else if (source instanceof MethodSource methodSource) {
                var testMethod = methodSource.getJavaMethod();
                var methodId = String.format("%s.%s(%s)",
                        methodSource.getClassName(),
                        methodSource.getMethodName(),
                        methodSource.getMethodParameterTypes());
                uniqueTests.add(methodId);
            }
        }

        // Index its children
        for (TestDescriptor child : test.getChildren())
            indexTestRecursive(child);
    }
}
