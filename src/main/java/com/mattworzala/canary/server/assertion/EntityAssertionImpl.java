package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.api.supplier.EntitySupplier;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class EntityAssertionImpl<S extends EntitySupplier, This extends EntityAssertionImpl<S, This>> extends AssertionImpl<S, This> {
    public EntityAssertionImpl(@NotNull S actual) {
        super(actual);
    }

    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public This toBeAt(@NotNull Point position) {
//        this.assertionTest = (T entity) -> sameBlock(entity.getPosition(), position);
        return self;
    }

    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public This toBeAt(@NotNull Supplier<@NotNull Point> pointSupplier) {
//        this.assertionTest = (T entity) -> sameBlock(entity.getPosition(), pointSupplier.get());
//
//        String assertionFormat = "Entity pos: %s\nOther pos: %s";
//        Function<Point, String> pointFormatter = (Point p) -> String.format("(%d, %d, %d)", p.blockX(), p.blockY(), p.blockY());
//        this.assertionFormatter = (T entity) -> {
//            String pointOne = pointFormatter.apply(entity.getPosition());
//            String pointTwo = pointFormatter.apply(pointSupplier.get());
//            return String.format(assertionFormat, pointOne, pointTwo);
//        };
        return self;
    }

    @NotNull
    @Contract(value = "-> this", mutates = "this")
    public This toBeRemoved() {
//        this.assertionTest = Entity::isRemoved;
        return self;
    }

    //todo move this to some util
//    public Boolean sameBlock(Point p1, Point p2) {
//        return p1.blockX() == p2.blockX() &&
//                p1.blockY() == p2.blockY() &&
//                p1.blockZ() == p2.blockZ();
//    }

}
