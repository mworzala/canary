package com.mattworzala.canary.internal.assertion;

import com.mattworzala.canary.internal.assertion.node.AeNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeTestNode extends AeNode {
    public static final AeTestNode PASSED = new AeTestNode(Result.PASS);
    public static final AeTestNode FAILED = new AeTestNode(Result.FAIL);

    private final Result result;

    public AeTestNode(Result result) {
        super(List.of());
        this.result = result;
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
