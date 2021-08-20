package com.mattworzala.canary.test.junit;

import com.mattworzala.canary.internal.EnvType;
import com.mattworzala.canary.internal.Environment;
import com.mattworzala.canary.test.junit.descriptor.CanaryEngineDescriptor;
import com.mattworzala.canary.test.junit.discovery.CanaryDiscoverer;
import com.mattworzala.canary.test.junit.execution.CanaryTestExecutor;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.*;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.platform.engine.TestExecutionResult.successful;

@Environment(EnvType.PLATFORM)
public class CanaryTestEngine implements TestEngine {
    private static final Logger logger = LoggerFactory.getLogger(CanaryTestEngine.class);

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
            runner.execute(iterator.next());
            iterator.remove();
        }
    }
}
