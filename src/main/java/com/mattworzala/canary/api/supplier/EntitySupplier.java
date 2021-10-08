package com.mattworzala.canary.api.supplier;

import net.minestom.server.entity.Entity;

@FunctionalInterface
public interface EntitySupplier<T extends Entity> {
    T get();

    default void doThing() {}

}
