package com.mattworzala.canary.codegen.genspec.supplier;

import com.mattworzala.canary.codegen.CanaryAnnotationProcessor;
import com.mattworzala.canary.codegen.RecursiveElementVisitor;
import com.mattworzala.canary.codegen.util.ElementUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.mattworzala.canary.codegen.PackageConstants.PKG_ASSERTION_IMPL;
import static com.mattworzala.canary.codegen.PackageConstants.PKG_ASSERTION_SPEC;

public class SupplierGenerator extends RecursiveElementVisitor<TypeSpec.Builder> {
    private final CanaryAnnotationProcessor.Logger logger;

    public SupplierGenerator(CanaryAnnotationProcessor.Logger logger) {
        this.logger = logger;
    }

    @Override
    public TypeSpec.Builder visitType(TypeElement element, TypeSpec.Builder builder) {
        AnnotationMirror genSpec = ElementUtil.getAnnotation(element, PKG_ASSERTION_SPEC + ".GenSpec");
        if (genSpec == null) {
            logger.error("GenSpec element must be annotated with @GenSpec", element);
            return null;
        }
        AnnotationValue operator = ElementUtil.getAnnotationMember(genSpec, "operator");
        if (operator == null) {
            logger.error("@GenSpec must contain a valid `operator` and `supertype`", element);
            return null;
        }

        TypeMirror operatorType = (TypeMirror) operator.getValue();

        String name = operatorType.toString(); //todo better way to get name
        TypeSpec.Builder typeSpec = TypeSpec.interfaceBuilder(name.substring(name.lastIndexOf(".") + 1) + "Supplier")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(FunctionalInterface.class);

        // Add the `get` method
        typeSpec.addMethod(MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ClassName.get(operatorType))
                .build());

        return typeSpec;
    }

    @Override
    public TypeSpec.Builder visitExecutable(ExecutableElement element, TypeSpec.Builder typeSpec) {
        if (ElementUtil.hasAnnotation(element, PKG_ASSERTION_IMPL + ".GenSpec.Transition")) {
            parseTransition(element, typeSpec);
        }

        return super.visitExecutable(element, typeSpec);
    }

    private void parseTransition(ExecutableElement element, TypeSpec.Builder typeSpec) {
        if (!isValidTransition(element)) {
            logger.error("@Transition methods must fit the criteria outlined in the @Transition Javadoc.", element);
            return;
        }

        VariableElement firstParameter = element.getParameters().get(0);

        TypeName toType = TypeName.get(element.getReturnType());
        TypeName fromType = TypeName.get(firstParameter.asType());

        var method = MethodSpec.methodBuilder(element.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(toType)
                .addParameter(fromType, firstParameter.getSimpleName().toString());

        method.addCode("return $T.$L($L)",
                ClassName.get(element.getEnclosingElement().asType()),
                element.getSimpleName(),
                firstParameter.getSimpleName());

        typeSpec.addMethod(method.build());
    }

    // public static ToType ...(FromType it)
    private static boolean isValidTransition(ExecutableElement element) {
        //todo check ToType and FromType
        return element.getParameters().size() == 1 &&
                element.getModifiers().contains(Modifier.PUBLIC) &&
                element.getModifiers().contains(Modifier.STATIC);
    }
}
