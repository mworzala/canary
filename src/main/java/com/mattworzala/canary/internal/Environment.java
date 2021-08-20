package com.mattworzala.canary.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines which environment (classloader) a type may be accessed from.
 * <p>
 * This exists only has a hint to the developer.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Environment {
    EnvType value();
}
