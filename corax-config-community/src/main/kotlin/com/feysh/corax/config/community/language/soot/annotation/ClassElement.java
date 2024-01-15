package com.feysh.corax.config.community.language.soot.annotation;

/**
 * Represents class constant elements.
 * We uses {@code String} instead of {@code Type} to represent the type
 * information of class element for the same reason as {@link Annotation}.
 */
public record ClassElement(String classDescriptor) implements Element {

    @Override
    public String toString() {
        return classDescriptor + ".class";
    }
}
