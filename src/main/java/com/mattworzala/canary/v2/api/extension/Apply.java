package com.mattworzala.canary.v2.api.extension;

import java.lang.annotation.*;

/**
 * Used to apply a plugin to a test element.
 */

@Repeatable(Apply.List.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Apply {
    Class<? extends Extension> value();

    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        Apply[] value();
    }
}
