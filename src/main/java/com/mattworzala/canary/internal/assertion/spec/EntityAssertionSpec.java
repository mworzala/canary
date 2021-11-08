package com.mattworzala.canary.internal.assertion.spec;

import com.mattworzala.canary.api.supplier.PointSupplier;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;

import static com.mattworzala.canary.internal.assertion.spec.GenSpec.*;

@GenSpec(operator = Entity.class, supertype = "Assertion")
@Supplier(transitions = {
        @Transition(name = "instance", target = InstanceAssertionSpec.class),
})
//@Transition(name = "instance", type = InstanceAssertionSpec.class)
public class EntityAssertionSpec {

//    @Condition
//    public static boolean toBeAt(Entity actual, double x, double y, double z) {
////        context.createErrorHandler((handler) -> {
////            handler.addMarker(new Vec(x, y, z), 0xFF0000, "Not Reached!");
////            return "Expected " + actual.getUuid() + " to reach " + expected;
////        }, actual, "<" + x + ", " + y + ", " + z + ">");
//        return actual.getPosition().sameBlock(expected);
//    }

    @Condition
    public static boolean toBeAt(Entity actual, Point expected) {
//        context.createErrorHandler((handler) -> {
//            handler.addMarker(expected, 0xFF0000, "Not Reached!");
//            return "Expected " + actual.getUuid() + " to reach " + expected;
//        } /* implicit capture of `actual` and `expected` */);
        return actual.getPosition().sameBlock(expected);
    }

    @Condition
    public static boolean toBeAt(Entity actual, PointSupplier expected) {
        return actual.getPosition().sameBlock(expected.get());
    }

    @Condition
    public static boolean toBeAtStrict(Entity actual, Point expected) {
        return actual.getPosition().samePoint(expected);
    }



}
