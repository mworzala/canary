package com.mattworzala.canary.internal.util.safety;

/**
 * Defines which environment (classloader) a type may be accessed from.
 * <p>
 * When a class is not annotated with {@link Env}, it is assumed to be Minestom and may not be accessed by any {@link #PLATFORM} or {@link #GLOBAL} classes.
 */
@Env(EnvType.GLOBAL)
public enum EnvType {
    /**
     * Specifies a type which may be accessed from both platform and global sources.
     * <p>
     * This means that the type must be loaded only in the platform classloader, however is protected in the Minestom classloader so access is acceptable.
     */
    GLOBAL,

    /**
     * Specifies a type which should not be accessed from the minestom classloader
     */
    PLATFORM,

    /**
     * Specifies a type which should not be accessed from the platform classloader
     */
    MINESTOM;
}
