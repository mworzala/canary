package com.mattworzala.canary.junit.runner;

import com.mattworzala.canary.junit.descriptor.TestDescription;
import com.mattworzala.canary.junit.support.Filterable;

public abstract class Filter {

    public static final Filter ALL = new Filter() {
        @Override
        public boolean shouldRun(TestDescription description) {
            return true;
        }

        @Override
        public String describe() {
            return "all tests";
        }

        @Override
        public void apply(Object child) {
            // do nothing
        }

        @Override
        public Filter intersect(Filter second) {
            return second;
        }
    };

//    public static Filter matchMethodDescription(final TestDescription desiredDescription) {
//        return new Filter() {
//            @Override
//            public boolean shouldRun(TestDescription description) {
//                if (description.isTest()) {
//                    return desiredDescription.equals(description);
//                }
//
//                // explicitly check if any children want to run
//                for (TestDescription each : description.getChildren()) {
//                    if (shouldRun(each)) {
//                        return true;
//                    }
//                }
//                return false;
//            }
//
//            @Override
//            public String describe() {
//                return String.format("Method %s", desiredDescription.getName());
//            }
//        };
//    }

    public abstract boolean shouldRun(TestDescription description);

    public abstract String describe();

    public void apply(Object child) {
        if (!(child instanceof Filterable)) {
            return;
        }
        Filterable filterable = (Filterable) child;
        filterable.filter(this);
    }

    public Filter intersect(final Filter second) {
        if (second == this || second == ALL) {
            return this;
        }
        final Filter first = this;
        return new Filter() {
            @Override
            public boolean shouldRun(TestDescription description) {
                return first.shouldRun(description)
                        && second.shouldRun(description);
            }

            @Override
            public String describe() {
                return first.describe() + " and " + second.describe();
            }
        };
    }
}
