package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeTestNode extends AeNode {
    public static final Result RES_PASS = Result.Pass();
    public static final Result RES_SOFT_PASS = Result.SoftPass();
    public static final Result RES_FAIL = Result.Fail("checked");

    public static final AeTestNode NODE_PASS = new AeTestNode(Result.Pass());
    public static final AeTestNode NODE_FAIL = new AeTestNode(RES_FAIL);
    public static final AeTestNode NODE_SOFT_PASS = new AeTestNode(Result.SoftPass());

    public Result result = RES_FAIL;

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
