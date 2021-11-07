package com.mattworzala.canary.codegen.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner14;

/**
 * @see <a href="https://stackoverflow.com/a/18777229/9842323">SO Answer</a>
 */
public class ImportScanner extends ElementScanner14<Void, Void> {
    private Multimap<String, Element> types = HashMultimap.create();

    public Multimap<String, Element> getImportedTypes() {
        return types;
    }

    @Override
    public Void visitType(TypeElement e, Void p) {
        for(TypeMirror interfaceType : e.getInterfaces()) {
            types.put(interfaceType.toString(), e);
        }
        types.put(e.getSuperclass().toString(), e);
        //todo Generics?
        return super.visitType(e, p);
    }

    //todo Records?

    @Override
    public Void visitExecutable(ExecutableElement e, Void p) {
        if(e.getReturnType().getKind() == TypeKind.DECLARED) {
            types.put(e.getReturnType().toString(), e);
        }
        return super.visitExecutable(e, p);
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement e, Void p) {
        if(e.asType().getKind() == TypeKind.DECLARED) {
            types.put(e.asType().toString(), e);
        }
        return super.visitTypeParameter(e, p);
    }

    @Override
    public Void visitVariable(VariableElement e, Void p) {
        if(e.asType().getKind() == TypeKind.DECLARED) {
            types.put(e.asType().toString(), e);
        }
        return super.visitVariable(e, p);
    }

}
