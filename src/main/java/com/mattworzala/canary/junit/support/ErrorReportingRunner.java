package com.mattworzala.canary.junit.support;

import com.mattworzala.canary.junit.descriptor.TestDescription;
import com.mattworzala.canary.junit.runner.Runner;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

public class ErrorReportingRunner extends Runner {
    private final List<Throwable> causes;

    private final String classNames;

    public ErrorReportingRunner(Class<?> testClass, Throwable cause) {
        this(cause, testClass);
    }

    public ErrorReportingRunner(Throwable cause, Class<?>... testClasses) {
        if (testClasses == null || testClasses.length == 0) {
            throw new NullPointerException("Test classes cannot be null or empty");
        }
        for (Class<?> testClass : testClasses) {
            if (testClass == null) {
                throw new NullPointerException("Test class cannot be null");
            }
        }
        classNames = getClassNames(testClasses);
        causes = getCauses(cause);
    }

    @Override
    public TestDescription getDescription() {
        TestDescription description = TestDescription.createSuiteDescription(classNames);
        for (Throwable each : causes) {
            description.addChild(describeCause());
        }
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        for (Throwable each : causes) {
            runCause(each, notifier);
        }
    }

    private String getClassNames(Class<?>... testClasses) {
        final StringBuilder builder = new StringBuilder();
        for (Class<?> testClass : testClasses) {
            if (builder.length() != 0) {
                builder.append(", ");
            }
            builder.append(testClass.getName());
        }
        return builder.toString();
    }

    @SuppressWarnings("deprecation")
    private List<Throwable> getCauses(Throwable cause) {
        if (cause instanceof InvocationTargetException) {
            return getCauses(cause.getCause());
        }
        //todo
//        if (cause instanceof InvalidTestClassError) {
//            return Collections.singletonList(cause);
//        }
//        if (cause instanceof InitializationError) {
//            return ((InitializationError) cause).getCauses();
//        }
//        if (cause instanceof org.junit.internal.runners.InitializationError) {
//            return ((org.junit.internal.runners.InitializationError) cause)
//                    .getCauses();
//        }
        return Collections.singletonList(cause);
    }

    private TestDescription describeCause() {
        return TestDescription.createTestDescription(classNames, "initializationError");
    }

    private void runCause(Throwable child, RunNotifier notifier) {
        Description description = describeCause();
        notifier.fireTestStarted(description);
        notifier.fireTestFailure(new Failure(description, child));
        notifier.fireTestFinished(description);
    }
}
