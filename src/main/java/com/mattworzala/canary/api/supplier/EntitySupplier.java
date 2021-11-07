package com.mattworzala.canary.api.supplier;

import com.mattworzala.canary.api.error.StubSupplierException;
import net.minestom.server.entity.Entity;

@FunctionalInterface
public interface EntitySupplier extends ObjectSupplier {
    Entity get();

    default PosSupplier position() {
        throw new StubSupplierException("EntitySupplier#position");
    }

//    default InstanceSupplier instance() {
//        throw new StubSupplierException("EntitySupplier#instance");
//    }
}
