package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeTestNode extends AeNode {
    public static final AeTestNode PASS = new AeTestNode(Result.PASS);
    public static final AeTestNode FAIL = new AeTestNode(Result.FAIL);
    public static final AeTestNode SOFT_PASS = new AeTestNode(Result.SOFT_PASS);

    public Result result = Result.FAIL;

    public AeTestNode(Result result) {
        super(List.of());
        this.result = result;
    }

    public AeTestNode(List<AeNode> children) {
        super(children);
    }

    @Override
    protected @NotNull Result test(Object target) {
        return result;
    }

    @Override
    public String toString() {
        return "<test>";
    }
}
