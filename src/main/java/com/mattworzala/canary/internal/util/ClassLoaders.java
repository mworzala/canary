package com.mattworzala.canary.internal.util;

import com.mattworzala.canary.internal.util.safety.Env;
import com.mattworzala.canary.internal.util.safety.EnvType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Objects;

@Env(EnvType.GLOBAL)
@ApiStatus.Internal
public class ClassLoaders {
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaders.class);

    public static final ClassLoader DEFAULT = com.mattworzala.canary.internal.junit.CanaryTestEngine.class.getClassLoader();
    public static final ClassLoader MINESTOM = ClassLoaders.class.getClassLoader();//MinestomRootClassLoader.getInstance();

    @NotNull
    public static Class<?> loadClassRequired(ClassLoader classLoader, Class<?> target) {
        return loadClassRequired(classLoader, target.getName());
    }

    @NotNull
    public static Class<?> loadClassRequired(ClassLoader classLoader, String name) {
        return Objects.requireNonNull(loadClass(classLoader, name), "Unable to load class " + name);
    }

    @Nullable
    public static Class<?> loadClass(ClassLoader classLoader, Class<?> target) {
        return loadClass(classLoader, target.getName());
    }

    @Nullable
    public static Class<?> loadClass(ClassLoader classLoader, String className) {
        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            logger.warn(e, () -> "Failed to load class: " + className);
            return null;
        }
    }

    @Nullable
    public static Class<? extends Annotation> loadAnnotation(ClassLoader classLoader, Class<? extends Annotation> annotationClass) {
        return loadAnnotation(classLoader, annotationClass.getName());
    }

    @Nullable
    public static Class<? extends Annotation> loadAnnotation(ClassLoader classLoader, String annotationClass) {
        try {
            //noinspection unchecked
            return (Class<? extends Annotation>) Class.forName(annotationClass, true, classLoader);
        } catch (ClassNotFoundException e) {
            logger.warn(e, () -> "Failed to load annotation: " + annotationClass);
            return null;
        }
    }
}
