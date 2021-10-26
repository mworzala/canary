package com.mattworzala.canary.server.assertion.node;

import com.mattworzala.canary.server.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeAndNode extends AeNode.Binary {
    public AeAndNode(@NotNull List<AeNode> children) {
        super(children);
    }

    @Override
    protected @NotNull Result test(Object target) {
        Result left = lhs().evaluate(target), right = rhs().evaluate(target);
        if (left == Result.FAILED || right == Result.FAILED)
            return Result.FAILED;
        return Result.PASSED;
    }

    @Override
    public String toString() {
        return "(" + lhs() + " AND " + rhs() + ")";
    }
}
