package com.mattworzala.canary.api;

import com.mattworzala.canary.platform.util.safety.EnvType;
import com.mattworzala.canary.platform.util.safety.Env;
import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Env(EnvType.MINESTOM)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Testable
public @interface IWBeforeEach {

}
