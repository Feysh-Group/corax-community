package com.feysh.corax.config.community.language.soot.annotation;

public record DoubleElement(double value) implements Element {

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
