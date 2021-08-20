package com.mattworzala.canary.api;

import com.mattworzala.canary.server.assertion.AssertionImpl;
import com.mattworzala.canary.server.assertion.EntityAssertionImpl;
import com.mattworzala.canary.server.assertion.LivingEntityAssertionImpl;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;

// Recursive generics are pretty awful, but I don't see another way around it
public final class Assertion<T> extends AssertionImpl<T, Assertion<T>> {



    public static final class EntityAssertion<T extends Entity> extends EntityAssertionImpl<T, EntityAssertion<T>> { }

    public static final class LivingEntityAssertion<T extends LivingEntity> extends LivingEntityAssertionImpl<T, LivingEntityAssertion<T>> { }
}
