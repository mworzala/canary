package com.mattworzala.canary.internal.util.safety;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines which environment (classloader) a type may be accessed from.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@ApiStatus.Internal
public @interface Env {
    EnvType value();
}
