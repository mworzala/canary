package com.mattworzala.canary;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface TestEnvironment {
    @NotNull Instance getInstance();


    Point getPos(String name);
    Block getBlock(String name);



    // Assertions

    void passWhenEntityPresent(Entity entity, Point position);



    // Instance manipulation utilities

    default <T extends Entity> T spawnEntity(Supplier<T> constructor) {
        return spawnEntity(constructor, Pos.ZERO, null);
    }
    default <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position) {
        return spawnEntity(constructor, position, null);
    }
    <T extends Entity> T spawnEntity(Supplier<T> constructor, Pos position, Consumer<T> config);
}
