package com.mattworzala.canary.codegen.genspec;

import com.google.auto.service.AutoService;
import com.mattworzala.canary.codegen.CanaryAnnotationProcessor;
import com.mattworzala.canary.codegen.util.ElementUtil;
import com.squareup.javapoet.*;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import java.util.Collection;
import java.util.List;

import static com.mattworzala.canary.codegen.PackageConstants.*;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes(PKG_ASSERTION_SPEC + ".GenSpec.IntermediateAssertion")
public class AggregateProcessor extends CanaryAnnotationProcessor {

    @Override
    public void process(Collection<? extends Element> elements) {
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder("Assertions")
                .addModifiers(Modifier.PUBLIC);

        for (Element intermediateAssertionType : elements) {
            String intClassName = intermediateAssertionType.getSimpleName().toString();

            // Name without the `Impl` suffix
            String className = intClassName.substring(0, intClassName.length() - 4);
            TypeSpec.Builder assertionClass = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);


            // Superclass
            AnnotationMirror annotation = ElementUtil.getAnnotation(intermediateAssertionType, PKG_ASSERTION_SPEC + ".GenSpec.IntermediateAssertion");
            AnnotationValue operatorValue = ElementUtil.getAnnotationMember(annotation, "operator");
            ParameterizedTypeName superClass = ParameterizedTypeName.get(
                    ClassName.get(PKG_ASSERTION_IMPL, intClassName),           // AssertionImpl
                    ClassName.get((TypeMirror) operatorValue.getValue()),      // T
                    ClassName.get(PKG_API, "Assertions", className) // This
            );
            assertionClass.superclass(superClass);

            // Constructor todo duplicated in GenSpecProcessor
            ParameterSpec paramSupplier = ParameterSpec.builder(ClassName.get(PKG_SUPPLIER, "ObjectSupplier"), "supplier").build();
            ParameterSpec paramSteplist = ParameterSpec.builder(ParameterizedTypeName.get(
                    ClassName.get(List.class),
                    ClassName.get(PKG_ASSERTION, "AssertionStep")
            ), "steps").build();
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSupplier)
                    .addParameter(paramSteplist)
                    .addStatement("super($N, $N)", paramSupplier, paramSteplist);
            assertionClass.addMethod(constructor.build());

            typeSpec.addType(assertionClass.build());
        }

        emitFile(PKG_API, typeSpec.build());
    }
}
