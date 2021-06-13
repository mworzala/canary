package com.example;

import net.minestom.server.extensions.Extension;

public class ExampleExtension extends Extension {
    @Override
    public void initialize() {
        System.out.println("Hello from example!");
    }

    @Override
    public void terminate() {

    }
}
