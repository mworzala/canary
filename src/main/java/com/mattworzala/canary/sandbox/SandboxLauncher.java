package com.mattworzala.canary.sandbox;

import com.mattworzala.canary.test.junit.CanaryTestEngine;
import com.mattworzala.canary.test.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.test.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.test.sandbox.SandboxTestExecutor;
import net.minestom.server.Bootstrap;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

public class SandboxLauncher {
    public static void main(String[] args) throws Exception {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage(""))
                .filters((PostDiscoveryFilter) testDescriptor -> {
                    if (testDescriptor instanceof CanaryEngineDescriptor)
                        return FilterResult.included("Canary root");
                    if (!(testDescriptor instanceof CanaryTestDescriptor test)) {
                        return FilterResult.excluded("Not a canary test");
                    }
//                    System.out.println(test.getTestClass());
                    return FilterResult.included("n/a");
                })
                .build();
//        Launcher launcher = LauncherFactory.create(LauncherConfig.builder()
//                .enableTestEngineAutoRegistration(false)
//                .addTestEngines(engine)
//                .build());
//
//        var listener = new SummaryGeneratingListener();
//        launcher.registerTestExecutionListeners(listener);
//        launcher.registerTestExecutionListeners(new TestExecutionListener() {
//            @Override
//            public void testPlanExecutionStarted(TestPlan testPlan) {
//                TestExecutionListener.super.testPlanExecutionStarted(testPlan);
//            }
//
//            @Override
//            public void testPlanExecutionFinished(TestPlan testPlan) {
//                TestExecutionListener.super.testPlanExecutionFinished(testPlan);
//            }
//
//            @Override
//            public void dynamicTestRegistered(TestIdentifier testIdentifier) {
//                TestExecutionListener.super.dynamicTestRegistered(testIdentifier);
//            }
//
//            @Override
//            public void executionSkipped(TestIdentifier testIdentifier, String reason) {
//                TestExecutionListener.super.executionSkipped(testIdentifier, reason);
//            }
//
//            @Override
//            public void executionStarted(TestIdentifier testIdentifier) {
//                System.out.println("Execution started: " + testIdentifier.getDisplayName());
//                TestExecutionListener.super.executionStarted(testIdentifier);
//            }
//
//            @Override
//            public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
//                System.out.println("Execution finished: " + testIdentifier.getDisplayName());
//                TestExecutionListener.super.executionFinished(testIdentifier, testExecutionResult);
//            }
//
//            @Override
//            public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
//                TestExecutionListener.super.reportingEntryPublished(testIdentifier, entry);
//            }
//        });




//        Method discover = launcher.getClass().getDeclaredMethod("discover", LauncherDiscoveryRequest.class, String.class);
//        discover.setAccessible(true);
//        var discoveryResult = (LauncherDiscoveryResult) discover.invoke(launcher, request, "discovery");

//        var result = (CanaryEngineDescriptor) discoveryResult.getEngineTestDescriptor(engine);



        CanaryTestEngine engine = new CanaryTestEngine();
        var result = (CanaryEngineDescriptor) engine.discover(request, UniqueId.forEngine(engine.getId()));


        System.out.println(result);
        result.getChildren().forEach(child -> {
            System.out.println(child.getDisplayName());
        });
//        Method getEngineTestDescriptors = LauncherDiscoveryResult.class.getDeclaredMethod("getEngineTestDescriptors");
//        getEngineTestDescriptors.setAccessible(true);

        {
//            TestPlan plan = launcher.discover(request);
//            launcher.execute(plan);

        }

//        TestExecutionSummary summary = listener.getSummary();
//        summary.printTo(new PrintWriter(System.out));
//        java.util.logging.config.file', "${project.buildDir}/resources/test/logging-test.properties




        MinestomRootClassLoader classLoader = MinestomRootClassLoader.getInstance();
        // Protect junit
        classLoader.protectedPackages.add("org.junit");
        // Protect all packages besides `server`.
        classLoader.protectedPackages.add("com.mattworzala.canary.test");
        classLoader.protectedPackages.add("com.mattworzala.canary.sandbox");

        SandboxTestExecutor.init();

        Bootstrap.bootstrap("com.mattworzala.canary.server.SandboxServer", args);

        // Can do stuff here
    }
}
