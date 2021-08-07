package com.mattworzala.canary.test.junit.assertion.recursive;

import com.mattworzala.canary.test.junit.assertion.Assertion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class EntityAssertionR<T extends Entity, A extends EntityAssertionR<T, A>> extends AssertionR<T, A> {

    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public A toBeAt(@NotNull Point position) {
        //todo
        return (A) this;
    }

}
