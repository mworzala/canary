package com.mattworzala.canary.api;

import org.intellij.lang.annotations.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvironmentAction {
    @Pattern("[a-z][a-z_]*")
    String value();
}
