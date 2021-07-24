package com.mattworzala.canary.demo;

import com.mattworzala.canary.test.InWorldTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDemo {

    @Test
    public void helloWorld() {

    }

    @InWorldTest
    public void aMinestomTest() {
        assertEquals(1, 1);
    }

    @InWorldTest
    public void anotherMinestomTest() {
        System.out.println("Hello");
        assertEquals(1, 2);
    }
}
