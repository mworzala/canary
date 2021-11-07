package com.mattworzala.canary.server.assertion.spec;

import net.minestom.server.entity.LivingEntity;

import static com.mattworzala.canary.server.assertion.spec.GenSpec.*;

@GenSpec(operator = LivingEntity.class, supertype = "EntityAssertion")
@Supplier
public class LivingEntityAssertionSpec {

}
