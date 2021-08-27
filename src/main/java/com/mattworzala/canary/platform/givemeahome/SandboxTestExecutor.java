package com.mattworzala.canary.platform.givemeahome;

import com.mattworzala.canary.platform.reflect.PHeadlessServer;
import com.mattworzala.canary.server.assertion.AssertionResult;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

public class SandboxTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SandboxTestExecutor.class);

    // There must be a better way of handing this
    private Stack<Object> instances = new Stack<>();

    private final PHeadlessServer server;

    public SandboxTestExecutor(PHeadlessServer server) {
        this.server = server;
    }

    public void execute(TestDescriptor test) {


        //todo this should handle any CanaryTestDescriptor or CanaryEngineDescriptor
//        logger.info(() -> test.getUniqueId().toString());

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
                    target.invoke(instance, environment.instance());
                    var result = environment.startTesting().ordinal();
                    if (result == AssertionResult.FAIL.ordinal()) {
                        System.out.println("Test failed: " + test.getUniqueId());
                    } else if (result == AssertionResult.PASS.ordinal()) {
                        System.out.println("Test passed: " + test.getUniqueId());
                    } else {
                        System.out.println("TEST RESULT UNKNOWN: " + test.getUniqueId());
                    }
                } else target.invoke(instance);

                // Loop on environment to test conditions
                //todo

            } catch (InvocationTargetException possibleAssertionError) {
                // AssertionError is masked here
                var cause = possibleAssertionError.getCause();
                if (cause instanceof AssertionError assertionError)
                    System.out.println("Test failed: " + assertionError.getMessage());
                else {
                    var wrapped = new RuntimeException("Cannot execute test method " + methodSource.getMethodName() + ":", possibleAssertionError);
                    throw wrapped;
                }
                return;
            } catch (IllegalAccessException e) {
                var wrapped = new RuntimeException("Cannot execute test method " + methodSource.getMethodName() + ":", e);
                throw wrapped;
            }
        }

        // Execute children
        for (TestDescriptor child : test.getChildren()) {
            execute(child);
        }


        if (source instanceof ClassSource) {
            instances.pop();
        }

        System.out.println("TEST SUCCESSFUL " + test.getUniqueId());
    }
}
