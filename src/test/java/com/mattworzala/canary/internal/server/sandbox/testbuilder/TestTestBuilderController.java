package com.mattworzala.canary.internal.server.sandbox.testbuilder;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class TestTestBuilderController {

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testAddPlayer() {
        MinecraftServer.init();
        TestBuilderController controller = new TestBuilderController("test-builder");

        Player player = Mockito.mock(Player.class);
        controller.addPlayer(player);
        Mockito.verify(player).setInstance(eq(controller.getTestBuilderInstance()), any(Vec.class));
    }

}
