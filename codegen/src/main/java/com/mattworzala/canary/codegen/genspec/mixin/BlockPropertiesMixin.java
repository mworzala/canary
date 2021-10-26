package com.mattworzala.canary.codegen.genspec.mixin;

import com.google.common.base.CaseFormat;
import com.mattworzala.canary.codegen.genspec.GenSpecMixin;
import com.mattworzala.canary.codegen.util.IntegerUtil;
import com.squareup.javapoet.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.mattworzala.canary.codegen.PackageConstants.*;

public class BlockPropertiesMixin implements GenSpecMixin {

    /**
     * Contains all the game properties and their possible values.
     */
    private static final Map<String, Set<String>> ALL_PROPERTIES = new HashMap<>();

    public BlockPropertiesMixin() {
        init();
    }

    @Override
    public void apply(TypeElement apElement, TypeSpec.Builder typeSpec) {
        for (var propertyDescriptor : ALL_PROPERTIES.entrySet()) {
            String property = propertyDescriptor.getKey();
            Set<String> values = propertyDescriptor.getValue();

            MethodSpec propertyMethod;

            // Boolean
            if (values.size() == 2 && values.contains("true") && values.contains("false")) {
                propertyMethod = createPropertyFunction(
                        "toBe", property, null,
                        "toHaveProperty($S, $S)", property, "true"
                );
            }
            // Integer
            else if (!values.isEmpty() && values.stream().allMatch(IntegerUtil::isInt)) {
                int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
                for (String value : values) {
                    int intValue = Integer.parseInt(value);
                    min = Math.min(min, intValue);
                    max = Math.max(max, intValue);
                }

                // Add a jetbrains @Range annotation for IDE hinting
                AnnotationSpec annotation = AnnotationSpec.builder(ClassName.get(PKG_JB_ANNOTATION_JB, "Range"))
                        .addMember("from", "$L", min)
                        .addMember("to", "$L", max)
                        .build();

                ParameterSpec parameter = ParameterSpec.builder(TypeName.INT, "value")
                        .addAnnotation(annotation)
                        .build();

                propertyMethod = createPropertyFunction(
                        "toHave", property, parameter,
                        "toHaveProperty($S, $T.valueOf($N))", property, String.class, parameter
                );
            }
            // Special: facing > Direction
            else if (property.equals("facing")) {
                ParameterSpec parameter = ParameterSpec.builder(ClassName.get(Direction.class), "value").build();

                propertyMethod = createPropertyFunction(
                        "toBe", property, parameter,
                        "toHaveProperty($S, $N.name().toLowerCase())", property, parameter
                );
            }
            // General handling (left as string enum
            else {
                // Add a jetbrains @MagicConstants annotation for IDE hinting
                String contractFormat = "$S, ".repeat(values.size());
                contractFormat = contractFormat.substring(0, contractFormat.length() - 2);
                AnnotationSpec annotation = AnnotationSpec.builder(ClassName.get(PKG_JB_ANNOTATION_IJ, "MagicConstant"))
                        .addMember("stringValues", "{" + contractFormat + "}", values.toArray(new Object[0]))
                        .build();

                ParameterSpec parameter = ParameterSpec.builder(String.class, "value")
                        .addAnnotation(annotation)
                        .build();

                propertyMethod = createPropertyFunction(
                        "toBe", property, parameter,
                        "toHaveProperty($S, $N)", property, parameter
                );
            }

            typeSpec.addMethod(propertyMethod);
        }
    }

    private MethodSpec createPropertyFunction(@NotNull String prefix, @NotNull String name,
                                              @Nullable ParameterSpec parameter,
                                              @Nullable String statementFormat, Object... statementArgs) {
        String methodName = prefix + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name);
        MethodSpec.Builder method = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeVariableName.get("This"));

        // Configurable parameter
        if (parameter != null) {
            method.addParameter(parameter);
        }

        // Configurable statement
        if (statementFormat != null) {
            method.addStatement(statementFormat, statementArgs);
        }

        // Return statement
        method.addStatement("return (This) this");

        return method.build();
    }

    // *** Static Init ***

    private static boolean initialized = false;
    private static void init() {
        if (initialized) return;
        initialized = true;

        for (short stateId = 0; stateId < Short.MAX_VALUE; stateId++) {
            Block block = Block.fromStateId(stateId);
            if (block == null) continue;

            for (var property : block.properties().entrySet()) {
                var values = ALL_PROPERTIES.computeIfAbsent(property.getKey(), ignored -> new HashSet<>());
                values.add(property.getValue());
            }
        }
    }

}
