package com.mattworzala.canary.internal.assertion;

import com.mattworzala.canary.api.Assertions;
import com.mattworzala.canary.api.supplier.ObjectSupplier;
import com.mattworzala.canary.internal.assertion.node.AeNode;

import java.util.ArrayList;
import java.util.List;

public class ManualTest {
    public static void main(String[] args) {
        ObjectSupplier sup = () -> "abc";

        List<AssertionStep> steps = new ArrayList<>();

        new Assertions.Assertion(sup, steps)
                .toEqual("abc");

        AeNode root = new AeSimpleParser(steps).parse();

        System.out.println(root);
        System.out.println(root.evaluate(sup.get()) == Result.PASSED);

    }
}
