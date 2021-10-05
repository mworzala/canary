package com.mattworzala.canary.demo;

import com.mattworzala.canary.api.InWorldTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDemo {

//    @Test
    public void helloWorld() {

    }

    @InWorldTest
    public void aMinestomTest() {
//        assertEquals(1, 2);

//        var instance = MinecraftServer.getInstanceManager().getInstances().stream().findAny().get();
//        instance.loadChunk(0, 0).join();
//        assertEquals(Block.AIR, instance.getBlock(0, 100, 0));
//        assertEquals(Block.AIR, instance.getBlock(0, 20, 0));
    }

    @InWorldTest
    public void anotherMinestomTest() {
        System.out.println("Hello");
//        assertEquals(1, 2);
    }

    //todo(matt) there is a bug in the discovery logic - This class is being added to the engine descriptor not TestDemo's descriptor
    public static class MyOtherDemo {
        @InWorldTest
        public void innerTest() {
            System.out.println("INSIDE WOW");
        }
    }
}
