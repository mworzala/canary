package com.mattworzala.canary.internal.util.safety;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines which environment (classloader) a type may be accessed from.
 * <p>
 * When a class is not annotated, it is assumed to be Minestom and may not be accessed by any {@link EnvType#PLATFORM} or {@link EnvType#GLOBAL} classes.
 */
@Env(EnvType.GLOBAL)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Env {
    EnvType value();
}
