package com.mattworzala.canary.codegen.util;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;

public class ElementUtil {
    private ElementUtil() {}

    public static boolean hasAnnotation(Element element, String annotationClass) {
        for (var annotation : element.getAnnotationMirrors()) {
            Name name = ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName();
            if (name.contentEquals(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    public static AnnotationMirror getAnnotation(Element element, String annotationClass) {
        for (var annotation : element.getAnnotationMirrors()) {
            Name name = ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName();
            if (name.contentEquals(annotationClass)) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * Assumes that annotationClass has an inner class named List to represent the @Repeatable target.
     */
    public static List<AnnotationMirror> getAnnotations(TypeElement element, String annotationClass) {
        List<AnnotationMirror> annotations = new ArrayList<>();
        for (var annotation : element.getAnnotationMirrors()) {
            Name name = ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName();
            if (name.contentEquals(annotationClass)) {
                annotations.add(annotation);
            } else if (name.contentEquals(annotationClass + ".List")) {
                // `value` member contains a list of `annotationClass`
                AnnotationValue value = ElementUtil.getAnnotationMember(annotation, "value"); assert value != null;
                //noinspection unchecked
                annotations.addAll((List<AnnotationMirror>) value.getValue());
            }
        }
        return annotations;
    }

    public static AnnotationValue getAnnotationMember(AnnotationMirror annotation, String memberName) {
        for (var entry : annotation.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(memberName)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
