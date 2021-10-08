package com.mattworzala.canary.api.supplier;

import net.minestom.server.entity.LivingEntity;

@FunctionalInterface
public interface LivingEntitySupplier<T extends LivingEntity> extends EntitySupplier<T> {
    T get();
}
