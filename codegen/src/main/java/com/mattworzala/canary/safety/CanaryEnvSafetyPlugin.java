package com.mattworzala.canary.safety;

import com.google.auto.service.AutoService;
import com.sun.source.tree.*;
import com.sun.source.util.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * Checks @Env annotations to ensure that classes do not import anything which is unusable in that context.
 * <p>
 * Processing is done in two steps:
 * <ul>
 *     <li>BEGIN ANALYZE : Resolve @Env on every class, keeping track of which environment they are targeting.</li>
 *     <li>FINSH ANALYZE : Resolve all imports to Canary classes on files with @Env, checking if they are valid and rendering errors if not.</li>
 * </ul>
 */
@AutoService(Plugin.class)
public class CanaryEnvSafetyPlugin implements Plugin, TaskListener {
    private static final String ENV_ANNOTATION = "com.mattworzala.canary.internal.util.safety.Env";

    private static final List<String> IGNORED_CLASSES = List.of("Env", "EnvType",
            // Valid to use these Minestom classes anywhere.
            "MinestomRootClassLoader", "ExtensionManager", "MixinCodeModifier", "MixinServiceMinestom");

    // Constants to represent the environment states
    public enum EnvType {
        GLOBAL, PLATFORM, MINESTOM
    }

    private Trees trees;

    // Unique state
    private final Map<String, EnvType> envStates = new HashMap<>();
    private int apRound = 0;

    @Override
    public String getName() {
        return "CanarySafetyChecks";
    }

    @Override
    public void init(JavacTask task, String... args) {
        trees = Trees.instance(task);
        task.addTaskListener(this);

        System.out.println("Performing Canary safety checks...");
    }

    @Override
    public void started(TaskEvent e) {
        if (e.getKind() == TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND) {
            apRound++;
        }
    }

    @Override
    public void finished(TaskEvent e) {
        if (e.getSourceFile() == null) return;
        String fileName = e.getSourceFile().getName();

        //TODO: Confirm first TypeDecl != null

        // Do nothing for generated sources
        if (fileName.contains("generated")) {
            return;
        }

        if (e.getKind() == TaskEvent.Kind.ENTER && apRound == 1) {
            // Find all @Env annotations and parse their value
            e.getCompilationUnit().accept(new EnvAnnotationScanner(), e);
        } else if (e.getKind() == TaskEvent.Kind.ANALYZE) {
            doImportCheck(e);
        }
    }

    private class EnvAnnotationScanner extends TreeScanner<Void, TaskEvent> {

        @Override
        public Void visitAnnotation(AnnotationTree node, TaskEvent event) {
            // Get the FQCN of the annotation
            // To do this without using (extremely) unsafe APIs, we get the AP element for it.
            Element element = trees.getElement(trees.getPath(event.getCompilationUnit(), node));
            if (!(element instanceof TypeElement typeElement)) {
                // Exit early if this is not a type element.
                return super.visitAnnotation(node, event);
            }
            String annotationName = typeElement.getQualifiedName().toString();
            if (!annotationName.equals(ENV_ANNOTATION)) {
                return super.visitAnnotation(node, event);
            }

            // Get the first argument, or error if missing. This is before the stage javac has confirmed
            //  that the correct number of arguments are present so we cannot assume there will be one.
            if (node.getArguments().size() != 1) {
                trees.printMessage(Diagnostic.Kind.ERROR, "@Env annotations must have one argument named `value`.", node, event.getCompilationUnit());
                return super.visitAnnotation(node, event);
            }
            AssignmentTree tree = (AssignmentTree) node.getArguments().get(0); // Safe to cast since all annotation arguments are assignments (I think?)

            EnvType envType = switch (tree.getExpression().toString()) {
                case "EnvType.GLOBAL" -> EnvType.GLOBAL;
                case "EnvType.PLATFORM" -> EnvType.PLATFORM;
                case "EnvType.MINESTOM" -> EnvType.MINESTOM;
                default -> null;
            };
            if (envType == null) {
                trees.printMessage(Diagnostic.Kind.ERROR, "Invalid EnvType: " + tree.getExpression(), node, event.getCompilationUnit());
                return super.visitAnnotation(node, event);
            }

            // Cache this EnvType for the given class
            envStates.put(UNSAFE_getTopLevelClassName(event), envType);

            return super.visitAnnotation(node, event);
        }
    }

    private void doImportCheck(TaskEvent e) {
        String topLevelClassName = UNSAFE_getTopLevelClassName(e);
        EnvType fileEnvType = envStates.getOrDefault(topLevelClassName, EnvType.MINESTOM);

        for (ImportTree rawImport : e.getCompilationUnit().getImports()) {
            String rawImportName = rawImport.getQualifiedIdentifier().toString();
            String importName = rawImport.isStatic() ? rawImportName.substring(0, rawImportName.lastIndexOf(".")) : rawImportName;

            if (!(importName.startsWith("com.mattworzala") || importName.startsWith("net.minestom")) ||
                    IGNORED_CLASSES.contains(importName.substring(importName.lastIndexOf(".") + 1))) {
                continue;
            }

            EnvType importEnvType = envStates.getOrDefault(importName, EnvType.MINESTOM);
            if (!isValidImport(fileEnvType, importName, importEnvType)) {
                // Not valid, throw error
//                System.out.println("Cannot import " + importName + " (" + importEnvType + ") from " + fileEnvType);
                trees.printMessage(Diagnostic.Kind.ERROR,
                        "Cannot import " + importName + " (" + importEnvType + ") from " + fileEnvType,
                        rawImport, e.getCompilationUnit());
            }
        }
    }

    private boolean isValidImport(EnvType inside, String importName, EnvType importEnvType) {
        // Only MINESTOM can import from minestom
        if (importName.startsWith("net.minestom")) {
            return inside == EnvType.MINESTOM;
        }

        // All GLOBAL imports are allowed
        if (importEnvType == EnvType.GLOBAL) {
            return true;
        }

        // Otherwise we can only access things in our envtype
        return inside == importEnvType;
    }

    private String UNSAFE_getTopLevelClassName(TaskEvent event) {
        Optional<? extends Tree> firstClassDecl = event.getCompilationUnit().getTypeDecls().stream().filter(decl -> decl.getKind() == Tree.Kind.INTERFACE || decl.getKind() == Tree.Kind.CLASS || decl.getKind() == Tree.Kind.RECORD || decl.getKind() == Tree.Kind.ENUM || decl.getKind() == Tree.Kind.ANNOTATION_TYPE).findFirst();
        TypeElement topLevelClassElement = (TypeElement) trees.getElement(trees.getPath(event.getCompilationUnit(), firstClassDecl.get()));
        return topLevelClassElement.getQualifiedName().toString();
    }
}
