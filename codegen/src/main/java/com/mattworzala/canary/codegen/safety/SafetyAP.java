package com.mattworzala.canary.codegen.safety;

import com.google.auto.service.AutoService;
import com.google.common.collect.Multimap;
import com.mattworzala.canary.codegen.util.ElementUtil;
import com.mattworzala.canary.codegen.util.ImportScanner;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Set;

import static com.mattworzala.canary.codegen.PackageConstants.*;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes(PKG_SAFETY + ".Env")
public class SafetyAP extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        info("Safety AP");

        var maybeAnnotation = annotations.stream().findFirst();
        if (maybeAnnotation.isEmpty()) return false;
        TypeElement annotation = maybeAnnotation.get();

        for (Element type : roundEnv.getElementsAnnotatedWith(annotation)) {
            AnnotationMirror envAnnotation = ElementUtil.getAnnotation(type, PKG_SAFETY + ".Env");
            AnnotationValue envTypeValue = ElementUtil.getAnnotationMember(envAnnotation, "value");
            String envType = envTypeValue.getValue().toString();

            ImportScanner importScanner = new ImportScanner();
            type.accept(importScanner, null);
            Multimap<String, Element> imports = importScanner.getImportedTypes();

            switch (envType) {
                case "GLOBAL", "PLATFORM" -> {

                }
                case "MINESTOM" -> {
                    info(type.toString());
                    for (var entry : imports.entries()) {
                        String importedElement = entry.getKey();
                        info(importedElement);
                    }
                }
            }
        }

//        for (TypeElement annotation : annotations) {
//            info(annotation.toString());
//        }

        return true;
    }

    private void info(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}
