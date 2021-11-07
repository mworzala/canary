package com.mattworzala.canary.server.assertion.spec;

import java.lang.annotation.*;

/**
 * GenSpec is used to tell the code generator how to generate assertions
 * about an assertion and the properties it has.
 * <p>
 * The base class tells the generator that this class represents an assertion,
 * and the subclasses are specific options related to that assertion.
 * <p>
 * An element annotated with @GenSpec will generate an assertion with the same
 * name besides the `Spec` suffix. For example, `EntityAssertionSpec` > `EntityAssertion`.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenSpec {
    /**
     * The class which this assertion operates on.
     * <p>
     * todo It may be possible to remove this class and determine the type from the first method.
     *
     * @return The class of the supplier type of the assertion.
     */
    Class<?> operator();

    /**
     * The parent assertion class. Should be specified as the assertion name without `Spec` at the end.
     * For example, if the parent is `EntityAssertion`, then that should be the value of supertype.
     * <p>
     * It should never be empty.
     *
     * @return The parent assertion class.
     */
    String supertype();

    /**
     * Allows the assertion to include extra content defined by the generator.
     * <p>
     * Mixin content is a generator intrinsic, so the name must be one which
     * the generator supports.
     * <p>
     * One example of a mixin is the `block_properties` mixin, which injects a
     * named condition for every block property, all calling back to the
     * `toHaveProperty` condition.
     */
    @Repeatable(Mixin.List.class)
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @interface Mixin {
        String value();

        /**
         * Should not be used directly, but allows for multiple mixins to be specified.
         */
        @Target(ElementType.TYPE)
        @Retention(RetentionPolicy.SOURCE)
        @interface List {
            Mixin[] value();
        }
    }

    /**
     * Adds a condition to the assertion. The annotated must have the following properties:
     * <ul>
     *     <li>Public</li>
     *     <li>Static</li>
     *     <li>Returns `boolean`</li>
     *     <li>First argument matches the {@link GenSpec#operator()}.</li>
     * </ul>
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    @interface Condition {
        /**
         * Specifies the debug name of the condition. The value is a format string to be
         * passed into {@link java.text.MessageFormat}. The available arguments are all
         * the parameters besides the first one, in order. For example if a condition
         * is defined as:
         * <p>
         * `public static boolean toBeAt(Pos actual, int x, int y, int z) { ... }`
         * <p>
         * Then the available arguments would be `{0}=x`, `{1}=y`, and `{2}=z`.
         * <p>
         * If no value is provided then &lt;condition&gt; is used.
         *
         * @return The debug name for the condition
         */
        String value() default "<condition>";
    }

    /* Supplier & Transitions */

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @interface Supplier {
        String name() default "";

        Transition[] transitions() default {};
    }


    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @interface Transition {
        String name() default "";

        Class<?> target();
    }

}
