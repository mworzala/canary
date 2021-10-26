package com.mattworzala.canary.server.assertion.node;

import com.mattworzala.canary.server.assertion.Result;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class AeNode {
    private final List<AeNode> children;

    private final List<Result> history = new ArrayList<>();

    public AeNode(@NotNull List<AeNode> children) {
        this.children = Collections.unmodifiableList(children); // Defensive copy
    }

    @NotNull
    public Stream<Result> history() {
        return history.stream();
    }

    @NotNull
    public final Result evaluate(Object target) {
        Result result = test(target);
        history.add(result);
        return result;
    }

    /**
     * Functionally the same as {@link #evaluate(Object)}, however no history will be used and caches will be skipped.
     * <p>
     * By default, this calls the node test function. If a node implements caching on its own,
     * then this method must be overridden to bypass the cache.
     */
    @NotNull
    public Result sample(Object target) {
        return test(target);
    }

    @NotNull
    protected abstract Result test(Object target);

    protected AeNode getChild(int index) {
        return children.get(index);
    }

    @Override
    public String toString() {
        return "<ERROR>";
    }


    /**
     *
     */
    public static abstract class Unary extends AeNode {

        public Unary(@NotNull List<AeNode> children) {
            super(children);
        }

        @NotNull
        public AeNode item() {
            return getChild(0);
        }
    }

    /**
     *
     */
    public static abstract class Binary extends AeNode {

        public Binary(@NotNull List<AeNode> children) {
            super(children);
        }

        @NotNull
        public AeNode lhs() {
            return getChild(0);
        }

        @NotNull
        public AeNode rhs() {
            return getChild(1);
        }

    }
}
