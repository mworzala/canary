package com.mattworzala.canary.codegen;

public interface PackageConstants {

    // Canary
    String PKG_ASSERTION = "com.mattworzala.canary.server.assertion";
    String PKG_ASSERTION_SPEC = PKG_ASSERTION + ".spec";
    String PKG_ASSERTION_IMPL = PKG_ASSERTION + ".impl";

    String PKG_API = "com.mattworzala.canary.api";
    String PKG_SUPPLIER = PKG_API + ".supplier";

    String PKG_SAFETY = "com.mattworzala.canary.platform.util.safety";

    // Jetbrains Annotations
    String PKG_JB_ANNOTATION_JB = "org.jetbrains.annotations";
    String PKG_JB_ANNOTATION_IJ = "org.intellij.lang.annotations";
}
