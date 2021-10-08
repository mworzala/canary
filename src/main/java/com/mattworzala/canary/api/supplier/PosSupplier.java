package com.mattworzala.canary.api.supplier;

import net.minestom.server.coordinate.Pos;

@FunctionalInterface
public interface PosSupplier extends PointSupplier {
    Pos get();
}
