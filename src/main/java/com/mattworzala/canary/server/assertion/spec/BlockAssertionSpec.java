package com.mattworzala.canary.server.assertion.spec;

import net.minestom.server.instance.block.Block;

import static com.mattworzala.canary.server.assertion.spec.GenSpec.*;

@GenSpec(operator = Block.class, supertype = "Assertion")
@Supplier
@Mixin("block_properties")
public class BlockAssertionSpec {

    @Condition
    public static boolean toHaveProperty(Block actual, String property, String value) {
        return value.equals(actual.getProperty(property));
    }

}
