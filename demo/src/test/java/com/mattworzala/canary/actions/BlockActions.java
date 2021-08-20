package com.mattworzala.canary.actions;

import com.mattworzala.canary.api.EnvironmentAction;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Point;

public class BlockActions {
    @EnvironmentAction("press_button")
    public void pressButton(TestEnvironment env, Point blockPos) {
        //todo do something to env
    }
}
