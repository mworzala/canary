package com.mattworzala.canary.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InWorldTest {
    //todo could put these in their own module to only expose annotations and assertions to end users.
}
