package com.mattworzala.canary.server.givemeahome;

import net.minestom.server.instance.Instance;

// 1 per server, keeps track of each TestExecutor to run tests given to it, also manages creation and destruction of TestInstances.
//   Executing tests here will be a blocking operation (CanaryTestEngine#execute will be replaced with a single call here)
//   Returns when all test results have been reported
public class TestCoordinator {

    public boolean isHeadless() {
        return true;
    }

}
