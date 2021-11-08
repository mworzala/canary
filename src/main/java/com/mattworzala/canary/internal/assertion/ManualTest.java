package com.mattworzala.canary.internal.assertion;

import com.mattworzala.canary.api.Assertions;
import com.mattworzala.canary.api.supplier.ObjectSupplier;
import com.mattworzala.canary.internal.assertion.node.AeNode;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class ManualTest {
    public static void main(String[] args) {
        ObjectSupplier sup = () -> {
            Entity entity = new Entity(EntityType.ZOMBIE);
            return entity;
        };

        List<AssertionStep> steps = new ArrayList<>();

        new Assertions.EntityAssertion(sup, steps)
                .toBeAt(new Vec(0, 0, 0))
                .and()
                .toBeAt(new Vec(1, 1, 1));

        AeNode root = new AeSimpleParser(steps).parse();

        System.out.println(root);

        Result result = root.evaluate(null);
        System.out.println("PASS : " + result.isPass());
        System.out.println("SOFT_PASS : " + result.isSoftPass());
        System.out.println("FAIL : " + result.isFail());

        System.out.println();
        if (result instanceof Result.FailResult failure) {
            failure.printToStdout(true);
        }

    }
}
