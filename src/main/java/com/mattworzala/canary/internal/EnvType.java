package com.mattworzala.canary.internal;

/**
 * Defines which environment (classloader) a type may be accessed from.
 * <p>
 * This exists only has a hint to the developer.
 */
public enum EnvType {
    /**
     * Specifies a type which may be accessed from either platform.
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
