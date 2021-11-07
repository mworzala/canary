package com.mattworzala.canary.server.assertion.impl;

import com.mattworzala.canary.api.supplier.ObjectSupplier;
import com.mattworzala.canary.server.assertion.AssertionCondition;
import com.mattworzala.canary.server.assertion.AssertionStep;

import java.util.List;
import java.util.function.Predicate;

public class AssertionBase<T, This extends AssertionBase<T, This>> {
    protected final ObjectSupplier supplier;
    protected final List<AssertionStep> steps;

    public AssertionBase(ObjectSupplier supplier, List<AssertionStep> steps) {
        this.supplier = supplier;
        this.steps = steps;
    }

    protected void appendCondition(String debugName, Predicate<T> condition) {
        steps.add(new AssertionStep(AssertionStep.Type.CONDITION, new AssertionCondition(debugName, (o) -> condition.test((T) o))));
    }
}
