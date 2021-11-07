package com.mattworzala.canary.internal.assertion;

public interface Result {
    Result PASSED = new Result() {};
    Result FAILED = new Result() {};
}
