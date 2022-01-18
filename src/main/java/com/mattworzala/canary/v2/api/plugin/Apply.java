package com.mattworzala.canary.v2.api.plugin;

import java.lang.annotation.*;

/**
 * Used to apply a plugin to a test element.
 */

@Repeatable(Apply.List.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Apply {

    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        Apply[] value();
    }
}
