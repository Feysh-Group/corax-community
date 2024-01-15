package com.feysh.corax.config.community.language.soot.annotation;

public record AnnotationElement(Annotation annotation) implements Element {

    @Override
    public String toString() {
        return annotation().toString();
    }
}
