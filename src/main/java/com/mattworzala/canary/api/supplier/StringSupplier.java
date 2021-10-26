package com.mattworzala.canary.api.supplier;

@FunctionalInterface
public interface StringSupplier extends ObjectSupplier {
    String get();
}
