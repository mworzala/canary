package com.mattworzala.canary.test.junit.assertion;

import com.mattworzala.canary.test.junit.assertion.recursive.AssertionR;
import com.mattworzala.canary.test.junit.assertion.recursive.EntityAssertionR;
import com.mattworzala.canary.test.junit.assertion.recursive.LivingEntityAssertionR;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;

// Recursive generics are pretty awful, but I don't see another way around it
public final class Assertion<T> extends AssertionR<T, Assertion<T>> {



    public static final class EntityAssertion<T extends Entity> extends EntityAssertionR<T, EntityAssertion<T>> { }

    public static final class LivingEntityAssertion<T extends LivingEntity> extends LivingEntityAssertionR<T, LivingEntityAssertion<T>> { }
}
