package com.mattworzala.canary.server.assertion;

import com.mattworzala.canary.api.supplier.EntitySupplier;
import com.mattworzala.canary.api.supplier.LivingEntitySupplier;
import com.mattworzala.canary.api.supplier.PointSupplier;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class EntityAssertionImpl<T extends Entity, This extends EntityAssertionImpl<T, This>> extends AssertionImpl<T, This> {

    public EntityAssertionImpl(T input) {
        super(input);
    }

    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public This toBeAt(@NotNull Point position) {
        this.assertionTest = (T entity) -> sameBlock(entity.getPosition(), position);
        return (This) this;
    }

    //todo move this to some util
    public Boolean sameBlock(Point p1, Point p2) {
        return p1.blockX() == p2.blockX() &&
                p1.blockY() == p2.blockY() &&
                p1.blockZ() == p2.blockZ();
    }

    @NotNull
    public This toBeAt(@NotNull Supplier<@NotNull Point> pointSupplier) {
        this.assertionTest = (T entity) -> sameBlock(entity.getPosition(), pointSupplier.get());

        String assertionFormat = "Entity pos: %s\nOther pos: %s";
        Function<Point, String> pointFormatter = (Point p) -> String.format("(%d, %d, %d)", p.blockX(), p.blockY(), p.blockY());
        this.assertionFormatter = (T entity) -> {
            String pointOne = pointFormatter.apply(entity.getPosition());
            String pointTwo = pointFormatter.apply(pointSupplier.get());
            return String.format(assertionFormat, pointOne, pointTwo);
        };
        return self();
    }

    @NotNull
    public This toBeRemoved() {
        this.assertionTest = Entity::isRemoved;
        return self();
    }

}
