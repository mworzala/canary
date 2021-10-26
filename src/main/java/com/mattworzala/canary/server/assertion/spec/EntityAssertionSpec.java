package com.mattworzala.canary.server.assertion.spec;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;

import static com.mattworzala.canary.server.assertion.spec.GenSpec.*;

@GenSpec(supplierType = Entity.class, supertype = "Assertion")
public class EntityAssertionSpec {

    @Condition
    public static boolean toBeAt(Entity actual, Point expected) {
        return actual.getPosition().sameBlock(expected);
    }

    @Condition
    public static boolean toBeAtStrict(Entity actual, Point expected) {
        return actual.getPosition().samePoint(expected);
    }

}
