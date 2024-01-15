package com.feysh.corax.config.community.language.soot.annotation;

public record BooleanElement(boolean value) implements Element {

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
