package com.mattworzala.canary.api.supplier;

import net.minestom.server.instance.Instance;

@FunctionalInterface
public interface InstanceSupplier extends ObjectSupplier {
    Instance get();
}
