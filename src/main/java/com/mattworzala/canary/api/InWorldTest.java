package com.mattworzala.canary.api;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Environment(EnvType.MINESTOM)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Testable
public @interface InWorldTest {
    //todo could put these in their own module to only expose annotations and assertions to end users.
}
