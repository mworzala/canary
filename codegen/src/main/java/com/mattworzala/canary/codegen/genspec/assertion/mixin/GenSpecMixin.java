package com.mattworzala.canary.codegen.genspec.assertion.mixin;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

public interface GenSpecMixin {
    void apply(TypeElement apElement, TypeSpec.Builder typeSpec);
}
