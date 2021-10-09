package com.mattworzala.canary.api.supplier;

import net.minestom.server.entity.LivingEntity;

@FunctionalInterface
public interface LivingEntitySupplier extends EntitySupplier {
    LivingEntity get();
}
