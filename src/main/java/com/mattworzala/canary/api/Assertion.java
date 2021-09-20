package com.mattworzala.canary.api;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import com.mattworzala.canary.server.assertion.AssertionImpl;
import com.mattworzala.canary.server.assertion.EntityAssertionImpl;
import com.mattworzala.canary.server.assertion.LivingEntityAssertionImpl;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;

@Environment(EnvType.MINESTOM)
public final class Assertion<T> extends AssertionImpl<T, Assertion<T>> {


    public Assertion(T input) {
        super(input);
    }

    public static final class EntityAssertion<T extends Entity> extends EntityAssertionImpl<T, EntityAssertion<T>> {
        public EntityAssertion(T input) {
            super(input);
        }
    }

    public static final class LivingEntityAssertion<T extends LivingEntity> extends LivingEntityAssertionImpl<T, LivingEntityAssertion<T>> {
        public LivingEntityAssertion(T input) {
            super(input);
        }
    }
}
