package com.mattworzala.canary.api.supplier;

import net.minestom.server.coordinate.Point;

@FunctionalInterface
public interface PointSupplier {
    Point get();
}
