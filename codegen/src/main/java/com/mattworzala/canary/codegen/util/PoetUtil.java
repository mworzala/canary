package com.mattworzala.canary.codegen.util;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;

public class PoetUtil {
    private PoetUtil() {}

    public static TypeName getFirstTypeVariableBound(TypeVariableName typeVariable) {
        List<TypeName> bounds = typeVariable.bounds;
        if (bounds.size() == 0) {
            return TypeName.get(Object.class);
        }
        return bounds.get(0);
    }
}
