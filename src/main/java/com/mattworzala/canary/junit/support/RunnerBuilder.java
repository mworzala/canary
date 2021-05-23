package com.mattworzala.canary.junit.support;

import com.mattworzala.canary.junit.descriptor.TestDescription;
import com.mattworzala.canary.junit.runner.Runner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class RunnerBuilder {
    private final Set<Class<?>> parents = new HashSet<Class<?>>();

    public abstract Runner runnerForClass(Class<?> testClass) throws Throwable;

    public Runner safeRunnerForClass(Class<?> testClass) {
        try {
            Runner runner = runnerForClass(testClass);
            if (runner != null) {
                configureRunner(runner);
            }
            return runner;
        } catch (Throwable e) {
            return new ErrorReportingRunner(testClass, e);
        }
    }

    private void configureRunner(Runner runner) {
        TestDescription description = runner.getDescription();
        OrderWith orderWith = description.getAnnotation(OrderWith.class);
        if (orderWith != null) {
            Ordering ordering = Ordering.definedBy(orderWith.value(), description);
            ordering.apply(runner);
        }
    }

    Class<?> addParent(Class<?> parent) {
        if (!parents.add(parent)) {
            throw new RuntimeException(String.format("class '%s' (possibly indirectly) contains itself as a SuiteClass", parent.getName()));
        }
        return parent;
    }

    void removeParent(Class<?> klass) {
        parents.remove(klass);
    }

    public List<Runner> runners(Class<?> parent, Class<?>[] children) {
        addParent(parent);

        try {
            return runners(children);
        } finally {
            removeParent(parent);
        }
    }

    public List<Runner> runners(Class<?> parent, List<Class<?>> children) {
        return runners(parent, children.toArray(new Class<?>[0]));
    }

    private List<Runner> runners(Class<?>[] children) {
        List<Runner> runners = new ArrayList<Runner>();
        for (Class<?> each : children) {
            Runner childRunner = safeRunnerForClass(each);
            if (childRunner != null) {
                runners.add(childRunner);
            }
        }
        return runners;
    }

    public @interface OrderWith {
        Class<? extends Ordering.Factory> value();
    }
}
