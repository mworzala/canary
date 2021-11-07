package com.mattworzala.canary.codegen.genspec;

import com.mattworzala.canary.codegen.genspec.mixin.BlockPropertiesMixin;
import com.mattworzala.canary.codegen.util.ElementUtil;
import com.squareup.javapoet.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor14;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.mattworzala.canary.codegen.PackageConstants.*;

//todo testing? https://github.com/google/compile-testing

/**
 * Processes GenSpec.* annotations, creating the relevant methods in the given {@link com.squareup.javapoet.JavaFile}.
 */
public class GenSpecProcessor extends SimpleElementVisitor14<TypeSpec.Builder, TypeSpec.Builder> {
    private static final Map<String, Supplier<GenSpecMixin>> MIXINS = new HashMap<>(){{
        put("block_properties", BlockPropertiesMixin::new);
    }};

    private final Messager messager;
    private final Consumer<TypeSpec> typeSpecEmitter;

    public GenSpecProcessor(Messager messager, Consumer<TypeSpec> typeSpecEmitter) {
        this.messager = messager;
        this.typeSpecEmitter = typeSpecEmitter;
    }

    /**
     * Creates the javapoet TypeSpec for this GenSpec
     */
    @Override
    public TypeSpec.Builder visitType(TypeElement element, TypeSpec.Builder ignored) {
        String name = element.getSimpleName().toString().replace("Spec", "Impl");
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC);

        AnnotationMirror genSpec = ElementUtil.getAnnotation(element, PKG_ASSERTION_SPEC + ".GenSpec");
        if (genSpec == null) {
            error("GenSpec element must be annotated with @GenSpec", element);
            return null;
        }
        AnnotationValue operator = ElementUtil.getAnnotationMember(genSpec, "operator");
        AnnotationValue superTypeName = ElementUtil.getAnnotationMember(genSpec, "supertype");
        if (operator == null || superTypeName == null) {
            error("@GenSpec must contain a valid `operator` and `supertype`", element);
            return null;
        }

        // Generic types
        typeSpec.addTypeVariable(TypeVariableName.get("T", ClassName.get((TypeMirror) operator.getValue())));
        var thisType = ParameterizedTypeName.get(
                ClassName.get(PKG_ASSERTION_IMPL, name),
                TypeVariableName.get("T"), TypeVariableName.get("This"));
        typeSpec.addTypeVariable(TypeVariableName.get("This", thisType));

        // Supertype
        String supertype = (String) superTypeName.getValue();
        if (supertype.isEmpty()) { supertype = "AssertionBase"; }
        else { supertype = supertype + "Impl"; }
        typeSpec.superclass(ParameterizedTypeName.get(
                ClassName.get(PKG_ASSERTION_IMPL, supertype),
                TypeVariableName.get("T"), TypeVariableName.get("This")
        ));

        // Constructor
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
        typeSpec.addMethod(constructor.build());

        // Process children before mixins
        TypeSpec.Builder result = super.visitType(element, typeSpec);

        // Mixins
        for (var annotation : ElementUtil.getAnnotations(element, PKG_ASSERTION_SPEC + ".GenSpec.Mixin")) {
            AnnotationValue targetValue = ElementUtil.getAnnotationMember(annotation, "value"); assert targetValue != null;
            String mixinTarget = (String) targetValue.getValue();

            Supplier<GenSpecMixin> mixin = MIXINS.get(mixinTarget);
            if (mixin == null) {
                error("Unknown mixin '" + mixinTarget + "'", element);
                continue;
            }

            // Apply the mixin
            try {
                mixin.get().apply(element, result);
            } catch (Throwable error) {
                StringWriter stringWriter = new StringWriter();
                error.printStackTrace(new PrintWriter(stringWriter));
                error("Failed to apply mixin '" + mixinTarget + "'!\n" + stringWriter, null);
            }
        }

        return result;
    }

    /**
     * Methods can be optionally tagged as generated elements (either transitions or conditions).
     *
     * @param element
     * @param typeSpec
     * @return
     */
    @Override
    public TypeSpec.Builder visitExecutable(ExecutableElement element, TypeSpec.Builder typeSpec) {
        if (!element.getModifiers().contains(Modifier.STATIC) || !element.getModifiers().contains(Modifier.PUBLIC))
            return super.visitExecutable(element, typeSpec);

        if (ElementUtil.hasAnnotation(element, PKG_ASSERTION_SPEC + ".GenSpec.Condition")) {
            parseCondition(element, typeSpec);
        } else {
            //todo probably can ignore this and only error if the method does not fit the requirements.
            error("public GenSpec methods must be @Condition.", element);
        }

        return super.visitExecutable(element, typeSpec);
    }

    private void parseCondition(ExecutableElement element, TypeSpec.Builder typeSpec) {
        // Get @Condition annotation
        AnnotationMirror condition = ElementUtil.getAnnotation(element, PKG_ASSERTION_SPEC + ".GenSpec.Condition"); assert condition != null;
        AnnotationValue debugString = ElementUtil.getAnnotationMember(condition, "value");
        String debugStringPattern = debugString == null ? "<condition>" : (String) debugString.getValue();

        // Generate method
        var method = MethodSpec.methodBuilder(element.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeVariableName.get("This"));

        // Parameters (all but first)
        element.getParameters()
                .stream().skip(1)
                .map(param -> ParameterSpec.builder(ClassName.get(param.asType()), param.getSimpleName().toString()).build())
                .forEach(method::addParameter);

        // Body (appendCondition call)
        // Map parameter names, skipping the first one (since that is "actual")
        String parameterString = element.getParameters().stream().skip(1).map(param -> param.getSimpleName().toString()).collect(Collectors.joining(", "));
        method.addStatement("appendCondition($T.format($S, $L), \nactual -> $T.$L(actual, $L))",
                // First argument (debug string)
                MessageFormat.class, debugStringPattern, parameterString,
                // Second argument (condition predicate)
                ClassName.get(element.getEnclosingElement().asType()), element.getSimpleName(), parameterString);
        method.addStatement("return (This) this");

        typeSpec.addMethod(method.build());
    }

    // *** Helpers ***

    @Override
    protected TypeSpec.Builder defaultAction(Element e, TypeSpec.Builder typeSpec) {
        e.getEnclosedElements().forEach(el -> el.accept(this, typeSpec));
        return typeSpec;
    }

    private void info(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    private void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void emitAdditonalTypeSpec(TypeSpec typeSpec) {
        typeSpecEmitter.accept(typeSpec);
    }
}
