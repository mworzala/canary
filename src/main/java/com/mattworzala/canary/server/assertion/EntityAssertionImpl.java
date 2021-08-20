package com.mattworzala.canary.server.assertion;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class EntityAssertionImpl<T extends Entity, A extends EntityAssertionImpl<T, A>> extends AssertionImpl<T, A> {

    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public A toBeAt(@NotNull Point position) {
        //todo
        return (A) this;
    }

}
