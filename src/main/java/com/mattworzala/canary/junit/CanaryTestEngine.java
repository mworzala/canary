package com.mattworzala.canary.junit;

import com.mattworzala.canary.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.junit.discovery.CanaryDiscoverer;
import com.mattworzala.canary.junit.execution.CanaryTestExecutor;
import org.junit.platform.engine.*;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.platform.engine.TestExecutionResult.successful;

public class CanaryTestEngine implements TestEngine {
    public static final String ID = "canary-test-engine";
    public static final String NAME = "Canary Test Engine";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of("com.mattworzala");
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of("canvas");
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of("0.0.1");
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        return CanaryDiscoverer.discover(discoveryRequest, uniqueId);
    }

    @Override
    public void execute(ExecutionRequest request) {
        EngineExecutionListener listener = request.getEngineExecutionListener();
        CanaryEngineDescriptor engineDescriptor = (CanaryEngineDescriptor) request.getRootTestDescriptor();
        listener.executionStarted(engineDescriptor);
        executeAllChildren(engineDescriptor, listener);
        listener.executionFinished(engineDescriptor, successful());
    }

    private void executeAllChildren(CanaryEngineDescriptor engineDescriptor, EngineExecutionListener listener) {
        CanaryTestExecutor runner = new CanaryTestExecutor(listener);
        Iterator<TestDescriptor> iterator = engineDescriptor.getChildrenMutable().iterator();
        while (iterator.hasNext()) {
            runner.execute((CanaryTestDescriptor) iterator.next());
            iterator.remove();
        }
    }

    //    @Override
//    public void execute(ExecutionRequest request) {
//        TestDescriptor engineDescriptor = request.getRootTestDescriptor();
//        EngineExecutionListener listener = request.getEngineExecutionListener();
//        listener.executionStarted(engineDescriptor);
//
//        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
//            listener.executionStarted(testDescriptor);
//
//            listener.executionFinished(testDescriptor, TestExecutionResult.successful());
//        }
//
//        listener.executionFinished(engineDescriptor, TestExecutionResult.successful());
//    }

    //    @Override
//    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
//        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "Canary Test Engine");
//        //todo DirectorySelector and ClassSelector also exist
//        discoveryRequest.getSelectorsByType(ClassSelector.class).forEach(selector -> {
//
//            engineDescriptor.addChild(new CanaryTestDescriptor(
//                    engineDescriptor.getUniqueId(),
//                    selector.getClassName(),
//                    Collections.singletonList(selector.getClassName())
//            ));
//        });
//        discoveryRequest.getSelectorsByType(FileSelector.class).forEach(selector -> {
//            try {
//                engineDescriptor.addChild(new CanaryTestDescriptor(
//                        engineDescriptor.getUniqueId(),
//                        selector.getFile().getName(),
//                        Files.readAllLines(selector.getFile().toPath())));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        engineDescriptor.addChild(new CanaryTestDescriptor(
//                engineDescriptor.getUniqueId(),
//                "TEST A",
//                Collections.singletonList("Abc")
//        ));
//        return engineDescriptor;
//    }
//
//    @Override
//    public void execute(ExecutionRequest request) {
//        TestDescriptor engineDescriptor = request.getRootTestDescriptor();
//        EngineExecutionListener listener = request.getEngineExecutionListener();
//        listener.executionStarted(engineDescriptor);
//
//        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
//            CanaryTestDescriptor descriptor = (CanaryTestDescriptor) testDescriptor;
//            listener.executionStarted(descriptor);
//
//            // Actual test logic
//            if (descriptor.getFileContent().get(0).equals("true")) {
//                listener.executionFinished(testDescriptor, TestExecutionResult.successful());
//            } else {
//                listener.executionFinished(testDescriptor, TestExecutionResult.failed(new AssertionError("File content was incorrect: " + descriptor.getFileContent().get(0) + " != \"true\"")));
//            }
//        }
//
//        //todo probably should base this on whether all the child tests passed?
//        listener.executionFinished(engineDescriptor, TestExecutionResult.successful());
//    }
}
