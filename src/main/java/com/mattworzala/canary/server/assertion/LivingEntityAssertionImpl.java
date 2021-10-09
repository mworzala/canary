package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.api.supplier.LivingEntitySupplier;
import org.jetbrains.annotations.NotNull;

public class LivingEntityAssertionImpl<S extends LivingEntitySupplier, This extends LivingEntityAssertionImpl<S, This>> extends EntityAssertionImpl<S, This> {
    public LivingEntityAssertionImpl(@NotNull S actual) {
        super(actual);
    }
}
