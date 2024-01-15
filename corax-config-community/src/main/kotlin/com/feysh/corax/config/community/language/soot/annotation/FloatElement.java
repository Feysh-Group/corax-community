package com.feysh.corax.config.community.language.soot.annotation;

public record FloatElement(float value) implements Element {

    @Override
    public String toString() {
        return Float.toString(value);
    }
}
