package com.mattworzala.canary.codegen.genspec.assertion;


import com.google.auto.service.AutoService;
import com.mattworzala.canary.codegen.CanaryAnnotationProcessor;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

import static com.mattworzala.canary.codegen.PackageConstants.*;

// New architecture
//  - Phase 1 = @GenSpec files get their suppliers and assertion impls generated
//  - Phase 2 = @Assertion files (generated impls) get aggregated.

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes(PKG_ASSERTION_SPEC + ".GenSpec")
public class AssertionProcessor extends CanaryAnnotationProcessor {
    @Override
    public void process(Element type) {
        // Attempt to run our GenSpec processor on the class and write the output.
        TypeSpec.Builder typeSpec = type.accept(new AssertionGenerator(logger), null);
        if (typeSpec == null) {
            return;
        } // Return immediately, we have already errored inside the processor.

        emitFile(PKG_ASSERTION_IMPL, typeSpec.build());
    }
}
