package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.api.supplier.ObjectSupplier;
import com.mattworzala.canary.internal.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class AeConditionNode extends AeNode {
    private final String debugName;
    private final ObjectSupplier supplier;
    private final Result.Predicate<Object> condition;

    public AeConditionNode(@NotNull String debugName, @NotNull ObjectSupplier supplier, @NotNull Result.Predicate<Object> condition) {
        super(List.of());
        this.debugName = debugName;
        this.supplier = supplier;
        this.condition = condition;
    }


    @Override
    protected @NotNull Result test(Object target) {
        //todo this does not work, we need more information from the test itself.
        //     Specifically error messages are not possible this way, it will need to be supplied by the assertion itself.

        try {
            return condition.test(supplier.get());
        } catch (Throwable throwable) {
            //todo Should be able to render the stacktrace, not just the message.
            return Result.Fail(throwable.getMessage());
        }
    }

    @Override
    public String toString() {
        return debugName;
    }
}
