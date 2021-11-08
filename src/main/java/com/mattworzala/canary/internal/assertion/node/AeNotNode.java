package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeNotNode extends AeNode.Unary {
    public AeNotNode(@NotNull List<AeNode> children) {
        super(children);
    }

    @Override
    protected @NotNull Result test(Object target) {
        Result result = item().evaluate(target);
        if (result == Result.PASS)
            return Result.FAIL;
        return Result.PASS;
    }

    @Override
    public String toString() {
        return "(NOT " + item() + ")";
    }
}
