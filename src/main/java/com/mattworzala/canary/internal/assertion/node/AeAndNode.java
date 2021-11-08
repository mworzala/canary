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
        if (left == Result.FAIL || right == Result.FAIL)
            return Result.FAIL;
        if (left == Result.SOFT_PASS || right == Result.SOFT_PASS)
            return Result.SOFT_PASS;
        return Result.PASS;
    }

    @Override
    public String toString() {
        return "(" + lhs() + " AND " + rhs() + ")";
    }
}
