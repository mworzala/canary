package com.mattworzala.canary.codegen.assertion;

import com.google.common.base.CaseFormat;
import com.mattworzala.canary.codegen.CodeGenerator;
import com.squareup.javapoet.*;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.intellij.lang.annotations.MagicConstant;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.function.Consumer;

public class AssertionImplGenerator implements CodeGenerator {


    @Override
    public void generate() {
        var spec = new AssertionImplSpec("BlockAssertionImpl",
                ClassName.get("com.mattworzala.canary.api.supplier", "BlockSupplier"),
                ClassName.get("com.mattworzala.canary.server.assertion", "AssertionImpl"));

        TypeName blockSupplier = spec.getSupplierType();
        ClassName assertionImpl = spec.getSuperAssertionType();
        ClassName blockAssertionImpl = ClassName.get("com.mattworzala.canary.server.assertion", spec.getName());

        TypeSpec.Builder type = TypeSpec.classBuilder(spec.getName())
                .addTypeVariable(TypeVariableName.get("S", blockSupplier))
                .addTypeVariable(TypeVariableName.get("This", ParameterizedTypeName.get(blockAssertionImpl, TypeVariableName.get("S"), TypeVariableName.get("This"))))
                .addSuperinterface(ParameterizedTypeName.get(assertionImpl, TypeVariableName.get("S"), TypeVariableName.get("This")));

        var constructorParam = ParameterSpec.builder(blockSupplier, "actual")
                .addAnnotation(NotNull.class)
                .build();
        var constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(constructorParam)
                .addStatement("super($N)", constructorParam)
                .build();
        type.addMethod(constructor);

        var assertGenericProperty = MethodSpec.methodBuilder("toHaveProperty")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Contract.class)
                        .addMember("value", "$S", "_, _ -> this")
                        .addMember("mutates", "this")
                        .build())
                .returns(TypeVariableName.get("This"))
                .addParameter(String.class, "property")
                .addParameter(String.class, "value")
                .addComment("TODO")
                .addStatement("return self")
                .build();
        type.addMethod(assertGenericProperty);

        Map<String, Set<String>> allProperties = new HashMap<>();

        /*
         Property types:
          - Enum: toBe{NAME} (eg toBeShape, toBeWest)
            - annotate with @MagicConstants
            - Exceptions
              - `facing` goes to minestom Direction
          - Bool: toBe{NAME} (eg toBeWaterlogged, toBeDisarmed)
          - int: toHave{NAME} (eg toHaveHoneyLevel, toHaveLayers)
            - annotate with @Range(min, max)
          -

          anything: toHaveProperty(String, String)
         */

        for (short i = 0; i < Short.MAX_VALUE; i++) {
            Block block = Block.fromStateId(i);
            if (block == null) continue;

            for (var property : block.properties().entrySet()) {
                var values = allProperties.computeIfAbsent(property.getKey(), ignored -> new HashSet<>());
                values.add(property.getValue());
            }
        }

        var singleArgContract = AnnotationSpec.builder(Contract.class)
                .addMember("value", "$S", "_ -> this")
                .addMember("mutates", "this")
                .build();

        for (var property : allProperties.entrySet()) {
            final String propertyName = property.getKey();
            final Set<String> propertyValues = property.getValue();

            String name;
            ParameterSpec parameter;
            Consumer<MethodSpec.Builder> statement;

            if (propertyValues.size() == 2 && propertyValues.contains("true") && propertyValues.contains("false")) { // Bool
                name = "toBe" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, propertyName);
                parameter = null;
                statement = m -> m.addStatement("toHaveProperty($S, $S)", propertyName, "true");
            } else if (!propertyValues.isEmpty() && propertyValues.stream().allMatch(this::isInt)) { // int
                int min = propertyValues.stream().mapToInt(Integer::parseInt).min().getAsInt();
                int max = propertyValues.stream().mapToInt(Integer::parseInt).max().getAsInt();

                name = "toHave" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, propertyName);
                parameter = ParameterSpec.builder(TypeName.INT, "value")
                        .addAnnotation(AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Range"))
                                .addMember("from", "$L", min)
                                .addMember("to", "$L", max)
                                .build())
                        .build();
                statement = m -> m.addStatement("toHaveProperty($S, $T.valueOf($N))", propertyName, String.class, parameter);
            } else if (propertyName.equals("facing")) { // EXCEPTION: `facing(Direction)`
                name = "toBeFacing";
                parameter = ParameterSpec.builder(ClassName.get(Direction.class), "value").build();
                statement = m -> m.addStatement("toHaveProperty($S, $L.name().toLowerCase())", propertyName, parameter);
            } else {
                String contractFormat = "$S, ".repeat(propertyValues.size());
                contractFormat = contractFormat.substring(0, contractFormat.length() - 2);

                name = "toBe" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, propertyName);
                parameter = ParameterSpec.builder(String.class, "value")
                        .addAnnotation(AnnotationSpec.builder(MagicConstant.class)
                                .addMember("stringValues", "{" + contractFormat + "}", (Object[]) propertyValues.toArray(new String[0]))
                                .build())
                        .build();
                statement = m -> m.addStatement("toHaveProperty($S, $N)", propertyName, parameter);
            }

            var method = MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeVariableName.get("This"));
            if (parameter != null)
                method.addParameter(parameter);
            statement.accept(method);
            method.addStatement("return self");

            type.addMethod(method.build());
        }


        var generatedFile = JavaFile.builder(blockAssertionImpl.packageName(), type.build())
                .build();

        try {
            generatedFile.writeTo(System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Contract()
    @Override
    public List<JavaFile> getEmittedFiles() {
        return null;
    }

    public static void main(String[] args) {
        new AssertionImplGenerator().generate();
    }
}
