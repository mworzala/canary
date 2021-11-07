package com.mattworzala.canary.codegen;

public interface PackageConstants {

    // Canary
    String PKG_INTERNAL = "com.mattworzala.canary.internal";

    String PKG_ASSERTION = PKG_INTERNAL + ".assertion";
    String PKG_ASSERTION_SPEC = PKG_ASSERTION + ".spec";
    String PKG_ASSERTION_IMPL = PKG_ASSERTION + ".impl";

    String PKG_API = "com.mattworzala.canary.api";
    String PKG_SUPPLIER = PKG_API + ".supplier";

    // Jetbrains Annotations
    String PKG_JB_ANNOTATION_JB = "org.jetbrains.annotations";
    String PKG_JB_ANNOTATION_IJ = "org.intellij.lang.annotations";
}
