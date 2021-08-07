package com.mattworzala.canary.actions;

import com.mattworzala.canary.test.EnvironmentAction;
import com.mattworzala.canary.test.TestEnvironment;
import net.minestom.server.coordinate.Point;

public class BlockActions {
    @EnvironmentAction("press_button")
    public void pressButton(TestEnvironment env, Point blockPos) {
        //todo do something to env
    }
}
