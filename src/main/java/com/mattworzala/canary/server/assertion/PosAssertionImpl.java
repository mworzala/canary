package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.api.supplier.PosSupplier;
import org.jetbrains.annotations.NotNull;

public class PosAssertionImpl<S extends PosSupplier, This extends PosAssertionImpl<S, This>> extends PointAssertionImpl<S, This> {
    public PosAssertionImpl(@NotNull S actual) {
        super(actual);
    }
}
