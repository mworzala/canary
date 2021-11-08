package com.mattworzala.canary.internal.assertion.spec;

import com.mattworzala.canary.api.supplier.PointSupplier;
import com.mattworzala.canary.internal.assertion.Result;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;

import static com.mattworzala.canary.internal.assertion.spec.GenSpec.*;

@GenSpec(operator = Entity.class, supertype = "Assertion")
public class EntityAssertionSpec {

    // TODO : Allow @Doc on transitions
    @Transition
    public static Instance instance(Entity entity) {
        return entity.getInstance();
    }

//    @Condition
//    public static boolean toBeAt(Entity actual, double x, double y, double z) {
////        context.createErrorHandler((handler) -> {
////            handler.addMarker(new Vec(x, y, z), 0xFF0000, "Not Reached!");
////            return "Expected " + actual.getUuid() + " to reach " + expected;
////        }, actual, "<" + x + ", " + y + ", " + z + ">");
//        return actual.getPosition().sameBlock(expected);
//    }

    @Condition("entity@{0}")
    public static Result toBeAt(Entity actual, Point expected) {
        if (actual.getPosition().sameBlock(expected)) {
            return Result.Pass();
        }
        return Result.Fail("Expected " + actual.getUuid() + " to reach " + expected)
                .withMarker(expected, 0xFF0000, "Not Reached!");
    }

    @Condition
    public static Result toBeAt(Entity actual, PointSupplier expected) {
        if (actual.getPosition().sameBlock(expected.get()))
            return Result.Pass();
        return Result.Fail("TODO : Not Implemented");
    }

    @Condition
    public static Result toBeAtStrict(Entity actual, Point expected) {
        if (actual.getPosition().samePoint(expected))
            return Result.Pass();
        return Result.Fail("TODO : Not Implemented");
    }


}
