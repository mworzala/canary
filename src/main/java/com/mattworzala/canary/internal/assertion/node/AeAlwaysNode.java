package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeAlwaysNode extends AeNode.Unary {
    private Result cache = null;

    public AeAlwaysNode(@NotNull List<AeNode> children) {
        super(children);
    }

    //todo docs explaining why we cache only a failed result
    @Override
    protected @NotNull Result test(Object target) {
        if (cache == null || cache != Result.FAIL) {
            cache = sample(target);
        }
        return cache;
    }

    @Override
    public @NotNull Result sample(Object target) {
        Result proxy = item().evaluate(target);
        if (proxy == Result.FAIL)
            return Result.FAIL;
        return Result.SOFT_PASS;
    }

    @Override
    public String toString() {
        return "(ALWAYS " + item() + ")";
    }
}
