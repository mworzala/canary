package com.mattworzala.canary.internal.server.sandbox.testbuilder;

import com.mattworzala.canary.internal.util.ui.ItemBehavior;
import net.minestom.server.coordinate.Point;

public class MarkerItem implements ItemBehavior {
    private final TestBuilderController controller;

    public MarkerItem(TestBuilderController controller) {
        this.controller = controller;
    }

    @Override
    public void onRightClick(Point point) {
        controller.addMarker(point);
    }

    @Override
    public int getId() {
        return 1235;
    }
}
