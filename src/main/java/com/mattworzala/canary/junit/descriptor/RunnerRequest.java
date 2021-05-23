package com.mattworzala.canary.junit.descriptor;

import com.mattworzala.canary.junit.runner.Runner;
import com.mattworzala.canary.junit.support.Request;

public class RunnerRequest extends Request {
    private final Runner runner;

    RunnerRequest(Runner runner) {
        this.runner = runner;
    }

    @Override
    public Runner getRunner() {
        return runner;
    }
}
