package com.mattworzala.canary.test.junit.util;

import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.lang.annotation.Annotation;

public class ClassLoaders {
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaders.class);

    public static final MinestomRootClassLoader MINESTOM = MinestomRootClassLoader.getInstance();

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
