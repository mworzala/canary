package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.api.supplier.PointSupplier;
import org.jetbrains.annotations.NotNull;

public class PointAssertionImpl<S extends PointSupplier, This extends PointAssertionImpl<S, This>> extends AssertionImpl<S, This> {
    public PointAssertionImpl(@NotNull S actual) {
        super(actual);
    }
}
