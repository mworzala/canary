package com.mattworzala.canary.platform.util.hint;

import org.jetbrains.annotations.ApiStatus;

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
@Target({ElementType.TYPE, ElementType.PACKAGE})
@ApiStatus.Internal
public @interface Environment {
    EnvType value();
}
