package com.mattworzala.canary.test.junit.assertion.recursive;

import net.minestom.server.entity.LivingEntity;

public class LivingEntityAssertionR<T extends LivingEntity, A extends LivingEntityAssertionR<T, A>> extends EntityAssertionR<T, A> {

}
