package com.mattworzala.canary.server.assertion;

public interface Result {
    Result PASSED = new Result() {};
    Result FAILED = new Result() {};
}
