package com.mattworzala.canary.internal.assertion.impl;

import com.mattworzala.canary.api.supplier.ObjectSupplier;
import com.mattworzala.canary.internal.assertion.AssertionCondition;
import com.mattworzala.canary.internal.assertion.AssertionStep;
import com.mattworzala.canary.internal.assertion.Result;
import com.mattworzala.canary.internal.assertion.spec.EntityAssertionSpec;

import java.util.List;
import java.util.function.Predicate;

public class AssertionBase<T, This extends AssertionBase<T, This>> {
    protected final ObjectSupplier supplier;
    protected final List<AssertionStep> steps;

    public AssertionBase(ObjectSupplier supplier, List<AssertionStep> steps) {
        this.supplier = supplier;
        this.steps = steps;
    }

    protected void appendCondition(String debugName, Result.Predicate<T> condition) {
        steps.add(new AssertionStep(AssertionStep.Type.CONDITION, debugName, supplier, (o) -> condition.test((T) o)));
    }

    public This and() {
        steps.add(AssertionStep.AND);
        return (This) this;
    }
}
