package com.mattworzala.canary.server.assertion;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class EntityAssertionImpl<T extends Entity, A extends EntityAssertionImpl<T, A>> extends AssertionImpl<T, A> {

    public EntityAssertionImpl(T input) {
        super(input);
    }

    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public A toBeAt(@NotNull Point position) {
        this.assertionTest = (T entity) -> sameBlock(entity.getPosition(), position);
        return (A) this;
    }

    public Boolean sameBlock(Point p1, Point p2) {
        return p1.blockX() == p2.blockX() &&
                p1.blockY() == p2.blockY() &&
                p1.blockZ() == p2.blockZ();
    }

    @NotNull
    public A toBeAt(@NotNull Supplier<@NotNull Point> pointSupplier) {
        this.assertionTest = (T entity) -> sameBlock(entity.getPosition(), pointSupplier.get());
        String assertionFormat = "Entity pos: %s\nOther pos: %s";
        Function<Point, String> pointFormatter = (Point p) -> String.format("(%d, %d, %d)", p.blockX(), p.blockY(), p.blockY());
        this.assertionFormatter = (T entity) -> {
            String pointOne = pointFormatter.apply(entity.getPosition());
            String pointTwo = pointFormatter.apply(pointSupplier.get());
            return String.format(assertionFormat, pointOne, pointTwo);
        };
        return (A) this;
    }

}
