package com.mattworzala.canary.server.givemeahome;

import com.mattworzala.canary.server.instance.ViewerInstance;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public class SandboxTestCoordinator extends TestCoordinator {
    private final ViewerInstance sandboxViewer;

    public SandboxTestCoordinator() {
        sandboxViewer = new ViewerInstance();

        new TestExecutor(null);

//        var testExecutor1 = new TestExecutor(new Structure(new Vec(10, 10, 10)));
//        sandboxViewer.addMirror(testExecutor1.getInstance(), 1, 1, 0, 0);

//        var testExecutor3 = new TestExecutor(new Structure(new Vec(13, 5, 13)));
//        sandboxViewer.addMirror(testExecutor3.getInstance(), 2, 1, 0, 0);

//        var testExecutor2 = new TestExecutor(new Structure(new Vec(30, 25, 30)));
//        sandboxViewer.addMirror(testExecutor2.getInstance(), 1, 3, 0, 0);
//        sandboxViewer.addMirror(testExecutor2.getInstance(), 1, 4, 0, 1);
//        sandboxViewer.addMirror(testExecutor2.getInstance(), 2, 3, 1, 0);
//        sandboxViewer.addMirror(testExecutor2.getInstance(), 2, 4, 1, 1);
    }

    @Override
    public boolean isHeadless() {
        return false;
    }

    @NotNull
    public ViewerInstance getSandboxViewer() {
        return sandboxViewer;
    }
}
