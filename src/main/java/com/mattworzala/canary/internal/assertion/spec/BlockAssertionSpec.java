package com.mattworzala.canary.internal.assertion.spec;

import net.minestom.server.instance.block.Block;

import static com.mattworzala.canary.internal.assertion.spec.GenSpec.*;

@GenSpec(operator = Block.class, supertype = "Assertion")
@Mixin("block_properties")
public class BlockAssertionSpec {

    @Condition
    public static boolean toHaveProperty(Block actual, String property, String value) {
        return value.equals(actual.getProperty(property));
    }

}
