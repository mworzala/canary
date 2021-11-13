package com.mattworzala.canary.codegen;

import javax.lang.model.element.Element;
import javax.lang.model.util.SimpleElementVisitor14;

/**
 * Essentially mimic Scanner behavior, but it always returns the "additonal parameter"
 */
public class RecursiveElementVisitor<T> extends SimpleElementVisitor14<T, T> {

    @Override
    protected T defaultAction(Element e, T t) {
        e.getEnclosedElements().forEach(el -> el.accept(this, t));
        return t;
    }
}
