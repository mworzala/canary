package com.mattworzala.canary.api.error;

import org.jetbrains.annotations.NotNull;

public class StubSupplierException extends RuntimeException {
    public StubSupplierException(@NotNull String name) {
        super(name + " is not implemented on stub suppliers. Please use `TestEnvironment#get` to retrieve the supplier.");
    }
}
