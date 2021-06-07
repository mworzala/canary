package com.mattworzala.canary.demo;

import com.mattworzala.canary.test.junit.InWorldTest;
import org.junit.jupiter.api.Test;

public class TestDemo {

    @Test
    public void helloWorld() {

    }

    @InWorldTest
    public void aMinestomTest() {

    }

    @InWorldTest
    public void anotherMinestomTest() {
        System.out.println("Hello");
    }
}
