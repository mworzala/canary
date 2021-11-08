package com.mattworzala.canary.codegen.genspec.assertion;

import com.mattworzala.canary.codegen.CanaryAnnotationProcessor;
import com.mattworzala.canary.codegen.RecursiveElementVisitor;
import com.mattworzala.canary.codegen.genspec.assertion.mixin.BlockPropertiesMixin;
import com.mattworzala.canary.codegen.genspec.assertion.mixin.GenSpecMixin;
import com.mattworzala.canary.codegen.util.ElementUtil;
import com.mattworzala.canary.codegen.util.StringUtil;
import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner14;
import javax.lang.model.util.SimpleElementVisitor14;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.mattworzala.canary.codegen.PackageConstants.*;

//todo testing? https://github.com/google/compile-testing

/**
 * Processes GenSpec.* annotations, creating the relevant methods in the given {@link com.squareup.javapoet.JavaFile}.
 */
public class AssertionGenerator extends RecursiveElementVisitor<TypeSpec.Builder> {
    private static final Map<String, Supplier<GenSpecMixin>> MIXINS = new HashMap<>() {{
        put("block_properties", BlockPropertiesMixin::new);
    }};

    private final CanaryAnnotationProcessor.Logger logger;

    public AssertionGenerator(CanaryAnnotationProcessor.Logger logger) {
        this.logger = logger;
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
            logger.error("GenSpec element must be annotated with @GenSpec", element);
            return null;
        }
        AnnotationValue operator = ElementUtil.getAnnotationMember(genSpec, "operator");
        AnnotationValue superTypeName = ElementUtil.getAnnotationMember(genSpec, "supertype");
        if (operator == null || superTypeName == null) {
            logger.error("@GenSpec must contain a valid `operator` and `supertype`", element);
            return null;
        }

        TypeName operatorType = ClassName.get((TypeMirror) operator.getValue());

        // Add aggregate annotation
        typeSpec.addAnnotation(AnnotationSpec
                .builder(ClassName.get(PKG_ASSERTION_SPEC, "GenSpec", "IntermediateAssertion"))
                .addMember("operator", "$T.class", operatorType)
                .build());

        // Generic types
        typeSpec.addTypeVariable(TypeVariableName.get("T", operatorType));
        var thisType = ParameterizedTypeName.get(
                ClassName.get(PKG_ASSERTION_IMPL, name),
                TypeVariableName.get("T"), TypeVariableName.get("This"));
        typeSpec.addTypeVariable(TypeVariableName.get("This", thisType));

        // Supertype
        String supertype = (String) superTypeName.getValue();
        if (supertype.isEmpty()) {
            supertype = "AssertionBase";
        } else {
            supertype = supertype + "Impl";
        }
        typeSpec.superclass(ParameterizedTypeName.get(
                ClassName.get(PKG_ASSERTION_IMPL, supertype),
                TypeVariableName.get("T"), TypeVariableName.get("This")
        ));

        // Constructor todo duplicated in AggregateProcessor
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
            AnnotationValue targetValue = ElementUtil.getAnnotationMember(annotation, "value");
            assert targetValue != null;
            String mixinTarget = (String) targetValue.getValue();

            Supplier<GenSpecMixin> mixin = MIXINS.get(mixinTarget);
            if (mixin == null) {
                logger.error("Unknown mixin '" + mixinTarget + "'", element);
                continue;
            }

            // Apply the mixin
            try {
                mixin.get().apply(element, result);
            } catch (Throwable error) {
                StringWriter stringWriter = new StringWriter();
                error.printStackTrace(new PrintWriter(stringWriter));
                logger.error("Failed to apply mixin '" + mixinTarget + "'!\n" + stringWriter, null);
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
        if (ElementUtil.hasAnnotation(element, PKG_ASSERTION_SPEC + ".GenSpec.Condition")) {
            parseCondition(element, typeSpec);
        }

        return super.visitExecutable(element, typeSpec);
    }

    private void parseCondition(ExecutableElement element, TypeSpec.Builder typeSpec) {
        // Ensure the method looks valid
        if (!isValidCondition(element)) {
            logger.error("@Condition methods must fit the criteria outlined in the @Condition Javadoc.", element);
            return;
        }

        // Get @Condition annotation
        AnnotationMirror condition = ElementUtil.getAnnotation(element, PKG_ASSERTION_SPEC + ".GenSpec.Condition");
        assert condition != null;
        AnnotationValue debugString = ElementUtil.getAnnotationMember(condition, "value");
        String debugStringPattern = debugString == null ? "<condition>" : (String) debugString.getValue();

        // Generate method
        var method = MethodSpec.methodBuilder(element.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeVariableName.get("This"));

        // Parameters (all but first)
        element.getParameters()
                .stream().skip(1)
                .map(param -> {
                    //todo this is a hack to allow use of not-yet-generated supplier types. Need a better solution
//                    if (param.asType().toString().equals("PointSupplier")) {
//                        logger.info("I TRIGGERED");
//                        return ParameterSpec.builder(ClassName.get(PKG_SUPPLIER, "PointSupplier"), param.getSimpleName().toString()).build();
//                    }
                    return ParameterSpec.builder(ClassName.get(param.asType()), param.getSimpleName().toString()).build();
                })
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

        // Javadocs
        insertJavadocs(element, method);

        typeSpec.addMethod(method.build());
    }

    private void insertJavadocs(ExecutableElement element, MethodSpec.Builder method) {
        // body
        AnnotationMirror bodyDocAnnotation = ElementUtil.getAnnotation(element, PKG_ASSERTION_SPEC + ".GenSpec.Doc");
        if (bodyDocAnnotation != null) {
            String value = (String) ElementUtil.getAnnotationMember(bodyDocAnnotation, "value").getValue();
            method.addJavadoc(StringUtil.insertPTags(value.trim()) + "\n");
        }

        // Parameters
        for (int i = 0; i < element.getParameters().size(); i++) {
            VariableElement param = element.getParameters().get(i);
            AnnotationMirror paramDocAnnotation = ElementUtil.getAnnotation(param, PKG_ASSERTION_SPEC + ".GenSpec.Doc");

            if (paramDocAnnotation != null) {
                if (i == 0) {
                    // Ensure first parameter is *not* documented
                    logger.error("`actual` parameter may not have a Javadoc.", param);
                    return;
                }

                String value = (String) ElementUtil.getAnnotationMember(paramDocAnnotation, "value").getValue();
                method.addJavadoc("\n@param $L $L", param.getSimpleName(), value.trim());
            }
        }
    }

    // public static boolean ...(Operator actual, ...)
    private static boolean isValidCondition(ExecutableElement element) {
        //todo check first parameter type
        return element.getParameters().size() >= 1 &&
                element.getReturnType().getKind() == TypeKind.BOOLEAN &&
                element.getModifiers().contains(Modifier.PUBLIC) &&
                element.getModifiers().contains(Modifier.STATIC);
    }

}
