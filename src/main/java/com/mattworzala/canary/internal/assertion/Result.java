package com.mattworzala.canary.internal.assertion;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Result {

    /**
     * Represents a {@link java.util.function.Predicate} for {@link Result}s instead of bools.
     *
     * @param <T> The type being operated on.
     */
    @FunctionalInterface
    public interface Predicate<T> {
        Result test(T t);
    }


    // Result creation

    public static Result Pass() {
        return PASS;
    }
    private static final Result PASS = new Result() {
        public boolean isPass() { return true; }
    };

    public static Result SoftPass() {
        return SOFT_PASS;
    }
    private static final Result SOFT_PASS = new Result() {
        public boolean isSoftPass() { return true; }
    };


    public static FailResult Fail(String reason) {
        return Fail(reason, null);
    }

    public static FailResult Fail(String reason, Result cause) {
        return new FailResult(reason, cause);
    }


    // Result Impl

    private Result() {}

    public boolean isPass() { return false; }
    public boolean isFail() { return false; }
    public boolean isSoftPass() { return false; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result that)) return false;
        return isPass() == that.isPass() || isSoftPass() == that.isSoftPass() || isFail() == that.isFail();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPass(), isSoftPass(), isFail());
    }


    // Failure

    public static final class FailResult extends Result {
        private final String reason;

        private final Result cause;

        public FailResult(String reason, Result cause) {
            this.reason = reason;
            this.cause = cause;
        }

        @NotNull
        public String getReason() {
            return reason;
        }


        @NotNull
        public Result getCause() {
            return cause;
        }

        @Override
        public boolean isFail() {
            return true;
        }

        public FailResult withMarker(Point location, int color, String message) {
            // TODO : Implement
            return this;
        }

        public void printToStdout(boolean first) {
            System.out.println((first ? "" : "> ") + getReason());

            if (getCause() instanceof FailResult failCause) {
                failCause.printToStdout(false);
            }
        }

        @Override
        public String toString() {
            return "FAIL{reason=\"" + getReason() + "\"}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FailResult that)) return false;
            return getReason().equals(that.getReason()) && Objects.equals(getCause(), that.getCause());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getReason(), getCause());
        }
    }
}