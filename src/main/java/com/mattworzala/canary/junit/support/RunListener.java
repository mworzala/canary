package com.mattworzala.canary.junit.support;

import com.mattworzala.canary.junit.descriptor.TestDescription;

public class RunListener {

    public void testRunStarted(TestDescription description) throws Exception {
    }

    public void testRunFinished(Result result) throws Exception {
    }

    public void testSuiteStarted(TestDescription description) throws Exception {
    }

    public void testSuiteFinished(TestDescription description) throws Exception {
    }

    public void testStarted(TestDescription description) throws Exception {
    }

    public void testFinished(TestDescription description) throws Exception {
    }

    public void testFailure(Failure failure) throws Exception {
    }

    public void testAssumptionFailure(Failure failure) {
    }
    
    public void testIgnored(TestDescription description) throws Exception {
    }
}
