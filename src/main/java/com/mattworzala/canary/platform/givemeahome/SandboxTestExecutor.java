package com.mattworzala.canary.platform.givemeahome;

import com.mattworzala.canary.platform.reflect.PHeadlessServer;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.assertion.AssertionResult;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

@Environment(EnvType.PLATFORM)
public class SandboxTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SandboxTestExecutor.class);

    // There must be a better way of handing this
    private Stack<Object> instances = new Stack<>();

    private final PHeadlessServer server;
    private final TestExecutionListener listener;

    public SandboxTestExecutor(PHeadlessServer server, TestExecutionListener listener) {
        this.server = server;
        this.listener = listener;
    }

    public void execute(TestDescriptor test) {


        //todo this should handle any CanaryTestDescriptor or CanaryEngineDescriptor
//        logger.info(() -> test.getUniqueId().toString());
        listener.start(test);

        // Execute test
        TestSource source = test.getSource().orElse(null);
        if (source instanceof ClassSource classSource) {
            var target = classSource.getJavaClass();

            try {
                var primaryConstructor = target.getDeclaredConstructor();
                primaryConstructor.setAccessible(true);
                instances.push(primaryConstructor.newInstance());
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                //todo better handing of these exceptions, also there may be cases where we are happy to provide arguments to a constructor
                var wrapped = new RuntimeException("Cannot execute test class " + classSource.getClassName() + ":", e);
                listener.end(test, wrapped);
                return;
            }
        }
        if (source instanceof MethodSource methodSource) {
            var target = methodSource.getJavaMethod();

            // Create test environment
            var environment = server.createEnvironment();

            try {
                assert !instances.isEmpty();
                var instance = instances.peek();

                // Invoke test method with/without environment depending on method definition
                if (target.getParameterCount() == 1) {
//                    ClassLoader.getRe
//                    environment.loadWorldData(Paths.get("/home/awkinley/gitrepos/canary/demo/src/res/testWorld.json"), 0, 41, 0);
                    target.invoke(instance, environment.instance());
                    var result = environment.startTesting().ordinal(); // BLOCKING
                    if (result == AssertionResult.FAIL.ordinal()) {
                        listener.end(test, new AssertionError("Condition failed."));
                        System.out.println("Test failed: " + test.getUniqueId());
                    } else if (result == AssertionResult.PASS.ordinal()) {
                        listener.end(test);
                        System.out.println("Test passed: " + test.getUniqueId());
                    } else {
                        listener.end(test, new AssertionError("Condition unknown."));
                        System.out.println("TEST RESULT UNKNOWN: " + test.getUniqueId());
                    }
                    return;
                } else target.invoke(instance);

                // Loop on environment to test conditions
                //todo

            } catch (InvocationTargetException possibleAssertionError) {
                // AssertionError is masked here
                var cause = possibleAssertionError.getCause();
                if (cause instanceof AssertionError assertionError) {
                    listener.end(test, assertionError);
                    System.out.println("Test failed: " + assertionError.getMessage());
                } else {
                    var wrapped = new RuntimeException("Cannot execute test method " + methodSource.getMethodName() + ":", possibleAssertionError);
                    listener.end(test, wrapped);
//                    throw wrapped;
                }
                return;
            } catch (IllegalAccessException e) {
                var wrapped = new RuntimeException("Cannot execute test method " + methodSource.getMethodName() + ":", e);
                listener.end(test, wrapped);
            }
        }

        // Execute children
        for (TestDescriptor child : test.getChildren()) {
            execute(child);
        }


        if (source instanceof ClassSource) {
            instances.pop();
        }

        listener.end(test);
    }
}
