package com.mattworzala.canary.server.assertion.spec;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenSpec {
    Class<?> supplierType();
    String supertype();

    @Repeatable(Mixin.List.class)
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @interface Mixin {
        String value();

        @Target(ElementType.TYPE)
        @Retention(RetentionPolicy.SOURCE)
        @interface List {
            Mixin[] value();
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Condition {
        String value() default "<condition>";
    }


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Transition {

    }
}
