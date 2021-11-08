package com.mattworzala.canary.codegen.genspec.supplier;

import com.google.auto.service.AutoService;
import com.mattworzala.canary.codegen.CanaryAnnotationProcessor;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

import static com.mattworzala.canary.codegen.PackageConstants.*;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes(PKG_ASSERTION_SPEC + ".GenSpec")
public class SupplierProcessor extends CanaryAnnotationProcessor {
    @Override
    public void process(Element type) {
        // Attempt to run our GenSpec processor on the class and write the output.
        TypeSpec.Builder typeSpec = type.accept(new SupplierGenerator(logger), null);
        if (typeSpec == null) {
            return;
        } // Return immediately, we have already errored inside the processor.

        emitFile(PKG_SUPPLIER, typeSpec.build());
    }
}