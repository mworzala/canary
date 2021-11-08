package com.mattworzala.canary.internal.assertion;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed abstract class Result permits Result.PassResult, Result.FailResult {

    @FunctionalInterface
    public interface Predicate<T> {
        Result test(T t);
    }


    public static Result Pass() {
        return PassResult.HARD;
    }

    public static Result SoftPass() {
        return PassResult.SOFT;
    }


    public static FailResult Fail(String reason) {
        return Fail(reason, null);
    }

    public static FailResult Fail(String reason, Result cause) {
        return new FailResult(reason, cause);
    }







    
    public boolean isPass() { return false; }
    public boolean isFail() { return false; }
    public boolean isSoftPass() { return false; }


    @Nullable
    public Result getCause() {
        return null;
    }



    // TODO : Implement #equals





    public static final class PassResult extends Result {
        private static final Result SOFT = new PassResult(true);
        private static final Result HARD = new PassResult(false);

        private final boolean soft;

        private PassResult(boolean soft) {
            this.soft = soft;
        }

        @Override
        public boolean isPass() {
            return !soft;
        }

        @Override
        public boolean isSoftPass() {
            return soft;
        }
    }

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


        @Override
        public @Nullable Result getCause() {
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
    }
}