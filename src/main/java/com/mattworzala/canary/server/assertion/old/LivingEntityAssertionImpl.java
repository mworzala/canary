package com.mattworzala.canary.server.assertion.old;

import net.minestom.server.entity.LivingEntity;

public class LivingEntityAssertionImpl<T extends LivingEntity, A extends LivingEntityAssertionImpl<T, A>> extends EntityAssertionImpl<T, A> {

    public LivingEntityAssertionImpl(T input) {
        super(input);
    }
}
