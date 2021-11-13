package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeAndNode extends AeNode.Binary {
    public AeAndNode(@NotNull List<AeNode> children) {
        super(children);
    }

    @Override
    protected @NotNull Result test(Object target) {
        Result left = lhs().evaluate(target), right = rhs().evaluate(target);
        // Handle LHS/RHS failure individually for better reporting
        if (left.isFail())
            return Result.Fail("Expected left side to pass, but it failed", left);
        if (right.isFail())
            return Result.Fail("Expected right side to pass, but it failed", right);

        if (left.isSoftPass() || right.isSoftPass())
            return Result.SoftPass();
        return Result.Pass();
    }

    @Override
    public String toString() {
        return "(" + lhs() + " AND " + rhs() + ")";
    }
}
