package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.api.supplier.ObjectSupplier;
import org.jetbrains.annotations.NotNull;

public class AssertionImpl<S extends ObjectSupplier, This extends AssertionImpl<S, This>> {
    protected final This self;

    protected final S actual;

    public AssertionImpl(@NotNull S actual) {
        //noinspection unchecked
        this.self = (This) this;

        this.actual = actual;
    }
}
