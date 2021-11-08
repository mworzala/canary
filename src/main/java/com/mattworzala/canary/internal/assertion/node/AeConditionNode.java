package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.api.supplier.ObjectSupplier;
import com.mattworzala.canary.internal.assertion.AssertionCondition;
import com.mattworzala.canary.internal.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class AeConditionNode extends AeNode {
    private final String debugName;
    private final ObjectSupplier supplier;
    private final Predicate<Object> test;

    public AeConditionNode(@NotNull AssertionCondition condition) {
        super(List.of());

        this.debugName = condition.debugName();
    }

    @Override
    protected @NotNull Result test(Object target) {
        //todo this does not work, we need more information from the test itself.
        //     Specifically error messages are not possible this way, it will need to be supplied by the assertion itself.
        var predicate = Objects.requireNonNull(condition.test());

        try {
            return predicate.test(target) ? Result.PASS : Result.FAIL;
        } catch (Throwable throwable) {
            //todo failure should result in message with exception
            return Result.FAIL;
        }
    }

    @Override
    public String toString() {
        return condition.debugName();
    }
}
