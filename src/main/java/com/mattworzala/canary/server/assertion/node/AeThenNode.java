package com.mattworzala.canary.server.assertion.node;

import com.mattworzala.canary.server.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeThenNode extends AeNode.Binary {
    private boolean testingRight = false;

    public AeThenNode(@NotNull List<AeNode> children) {
        super(children);
    }

    @Override
    protected @NotNull Result test(Object target) {
        if (testingRight) {
            //todo docs on why we do not use cached right value
            return rhs().test(target);
        }
        Result left = lhs().evaluate(target);
        if (left == Result.PASSED) {
            testingRight = true;
            return rhs().test(target);
        }
        return Result.FAILED;
    }

    @Override
    public String toString() {
        return "(" + lhs() + " THEN " + rhs() + ")";
    }
}
