package com.mattworzala.canary.v2.api.extension;

/**
 * Provides an environment for the extension to run in.
 *
 * For example, a context provider may start a Minestom server which is used later by the InstanceProvider for @Instance members.
 */
public interface EnvironmentProvider extends Extension {

}
