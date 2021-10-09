package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.api.supplier.InstanceSupplier;
import org.jetbrains.annotations.NotNull;

public class InstanceAssertionImpl<S extends InstanceSupplier, This extends InstanceAssertionImpl<S, This>> extends AssertionImpl<S, This> {
    public InstanceAssertionImpl(@NotNull S actual) {
        super(actual);
    }
}
