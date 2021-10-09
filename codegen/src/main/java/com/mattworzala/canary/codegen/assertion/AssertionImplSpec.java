package com.mattworzala.canary.codegen.assertion;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;

public class AssertionImplSpec {
    private final String name;
    private final ClassName supplierType;
    private final ClassName superAssertionType;

    public AssertionImplSpec(String name, @NotNull ClassName supplierType, @NotNull ClassName superAssertionType) {
        this.name = name;
        this.supplierType = supplierType;
        this.superAssertionType = superAssertionType;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public TypeName getSupplierType() {
        return supplierType;
    }

    public ClassName getSuperAssertionType() {
        return superAssertionType;
    }
}
