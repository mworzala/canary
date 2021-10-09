package com.mattworzala.canary.api;

import com.mattworzala.canary.api.supplier.*;
import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.assertion.*;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.MINESTOM)
public final class Assertion extends AssertionImpl<ObjectSupplier, Assertion> {
    public Assertion(@NotNull ObjectSupplier input) {
        super(input);
    }

    // *** Coordinates ***

    public static final class PointAssertion extends PointAssertionImpl<PointSupplier, PointAssertion> {
        public PointAssertion(@NotNull PointSupplier actual) {
            super(actual);
        }
    }

    public static final class PosAssertion extends PosAssertionImpl<PosSupplier, PosAssertion> {
        public PosAssertion(@NotNull PosSupplier actual) {
            super(actual);
        }
    }

    // *** Entities ***

    public static final class EntityAssertion extends EntityAssertionImpl<EntitySupplier, EntityAssertion> {
        public EntityAssertion(@NotNull EntitySupplier actual) {
            super(actual);
        }
    }

    public static final class LivingEntityAssertion extends LivingEntityAssertionImpl<LivingEntitySupplier, LivingEntityAssertion> {
        public LivingEntityAssertion(@NotNull LivingEntitySupplier actual) {
            super(actual);
        }
    }

    // *** Instance ***

    public static final class InstanceAssertion extends InstanceAssertionImpl<InstanceSupplier, InstanceAssertion> {
        public InstanceAssertion(@NotNull InstanceSupplier actual) {
            super(actual);
        }
    }
}
