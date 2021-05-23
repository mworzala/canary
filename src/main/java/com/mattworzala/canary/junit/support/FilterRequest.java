package com.mattworzala.canary.junit.support;

import com.mattworzala.canary.junit.runner.Filter;
import com.mattworzala.canary.junit.runner.Runner;

public final class FilterRequest extends Request {
    private final Request request;
    private final Filter filter;

    public FilterRequest(Request request, Filter filter) {
        this.request = request;
        this.fFilter = filter;
    }

    @Override
    public Runner getRunner() {
        try {
            Runner runner = request.getRunner();
            filter.apply(runner);
            return runner;
        } catch (Exception e) { // todo NoTestsRemainException
            return new ErrorReportingRunner(Filter.class, new Exception(String
                    .format("No tests found matching %s from %s", fFilter
                            .describe(), request.toString())));
        }
    }
}
