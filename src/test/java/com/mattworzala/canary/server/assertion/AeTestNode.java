package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.server.assertion.node.AeNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AeTestNode extends AeNode {
    public static final AeTestNode PASSED = new AeTestNode(Result.PASSED);
    public static final AeTestNode FAILED = new AeTestNode(Result.FAILED);

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
