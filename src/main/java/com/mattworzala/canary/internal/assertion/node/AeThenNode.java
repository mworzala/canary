package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
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
            return rhs().evaluate(target);
        } else {
            Result left = lhs().evaluate(target);
            // once the left passes, we start testing the right, and fully evaluating
            // this means that in the case of (A THEN (ALWAYS B)) that B needs to be true on the same tick A is true for the first time
            if (left.isPass()) {
                testingRight = true;
                return rhs().evaluate(target);
            }
            // on soft pass we check to see if the rhs is "ready" for us to start evaluating it
            // this handles ((ALWAYS A) THEN B), by saying that A has to be true up to and include the tick where B is true
            // similarly in the case of ((ALWAYS A) THEN (ALWAYS B)), A must be true up to and including the first tick where B is true
            if (left.isSoftPass()) {
                Result right = rhs().test(target);
                if (!right.isFail()) {
                    testingRight = true;
                    return right;
                }
                // Otherwise continue testing left.
            }

            // if left FAILS, return FAIL
            return Result.Fail("TODO : Not Implemented", left);
        }
    }

    @Override
    public String toString() {
        return "(" + lhs() + " THEN " + rhs() + ")";
    }
}
