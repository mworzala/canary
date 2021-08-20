package com.mattworzala.canary.demo;

import com.mattworzala.canary.test.InWorldTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDemo {

    @Test
    public void helloWorld() {

    }

    @InWorldTest
    public void aMinestomTest() {
        assertEquals(1, 1);

        var instance = MinecraftServer.getInstanceManager().getInstances().stream().findAny().get();
        assertEquals(Block.AIR, instance.getBlock(0, 100, 0));
        assertEquals(Block.AIR, instance.getBlock(0, 20, 0));
    }

    @InWorldTest
    public void anotherMinestomTest() {
        System.out.println("Hello");
        assertEquals(1, 2);
    }

    public static class MyOtherDemo {
        @InWorldTest
        public void innerTest() {
            System.out.println("INSIDE WOW");
        }
    }
}
