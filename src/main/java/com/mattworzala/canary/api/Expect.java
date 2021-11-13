package com.mattworzala.canary.api;

import com.mattworzala.canary.api.supplier.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;

import static com.mattworzala.canary.api.Assertions.*;
import static com.mattworzala.canary.internal.execution.TestEnvironmentImpl.getActiveEnvironment;

public class Expect {
    // @formatter:off

    // Pos
    public static PosAssertion expect(PosSupplier actual) { return getActiveEnvironment().expect(actual); }
    public static PosAssertion expect(Pos actual) { return expect(() -> actual); }

    // Point/Vec
    public static PointAssertion expect(PointSupplier actual) { return getActiveEnvironment().expect(actual); }
    public static PointAssertion expect(Point actual) { return expect(() -> actual); }

    // LivingEntity
    public static LivingEntityAssertion expect(LivingEntitySupplier actual) { return getActiveEnvironment().expect(actual); }
    public static LivingEntityAssertion expect(LivingEntity actual) { return expect(() -> actual); }

    // Entity
    public static EntityAssertion expect(EntitySupplier actual) { return getActiveEnvironment().expect(actual); }
    public static EntityAssertion expect(Entity actual) { return expect(() -> actual); }

    // Instance
    public static InstanceAssertion expect(InstanceSupplier actual) { return getActiveEnvironment().expect(actual); }
    public static InstanceAssertion expect(Instance actual) { return expect(() -> actual); }

    // @formatter:on
}
