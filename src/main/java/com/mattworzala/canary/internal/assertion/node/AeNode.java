package com.mattworzala.canary.internal.assertion.node;

import com.mattworzala.canary.internal.assertion.Result;
import kotlin.NotImplementedError;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class AeNode {
    public static final AeNode MISSING = new AeNode(List.of()) {
        protected @NotNull Result test(Object target) { throw new IllegalStateException("Missing assertion node!"); }
        public String toString() { return "<!>"; }
    };

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
        if (index < children.size() && index >= 0)
            return children.get(index);
        return AeNode.MISSING;
    }

    @Override
    public abstract String toString();


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
