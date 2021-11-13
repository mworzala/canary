package com.mattworzala.canary.internal.assertion.spec;

import com.mattworzala.canary.internal.assertion.Result;
import net.minestom.server.instance.block.Block;

import static com.mattworzala.canary.internal.assertion.spec.GenSpec.*;

@GenSpec(operator = Block.class, supertype = "Assertion")
@Mixin("block_properties")
public class BlockAssertionSpec {

    @Condition
    public static Result toHaveProperty(Block actual, String property, String value) {
        if (value.equals(actual.getProperty(property)))
            return Result.Pass();
        return Result.Fail("TODO : Not Implemented");
    }

}
