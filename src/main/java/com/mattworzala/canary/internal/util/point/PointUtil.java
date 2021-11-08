package com.mattworzala.canary.internal.util.point;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

public class PointUtil {
    public static Point UNIT_VECTOR = new Vec(1.0, 1.0, 1.0);

    /**
     * Returns a new point where each component is the minimum of that component in the two given points
     *
     * @param p1
     * @param p2
     * @return
     */
    public static Point minOfPoints(Point p1, Point p2) {
        return new Vec(Math.min(p1.x(), p2.x()), Math.min(p1.y(), p2.y()), Math.min(p1.z(), p2.z()));
    }

    /**
     * Returns a new point where each component is the maximum of that component in the two given points
     *
     * @param p1
     * @param p2
     * @return
     */
    public static Point maxOfPoints(Point p1, Point p2) {
        return new Vec(Math.max(p1.x(), p2.x()), Math.max(p1.y(), p2.y()), Math.max(p1.z(), p2.z()));
    }
}
