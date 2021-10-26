package com.mattworzala.canary.server.assertion.node;

import com.mattworzala.canary.server.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeNotNode extends AeNode.Unary {
    public AeNotNode(@NotNull List<AeNode> children) {
        super(children);
    }

    @Override
    protected @NotNull Result test(Object target) {
        Result result = item().evaluate(target);
        if (result == Result.PASSED)
            return Result.FAILED;
        return Result.PASSED;
    }

    @Override
    public String toString() {
        return "(NOT " + item() + ")";
    }
}
