package com.feysh.corax.config.community.language.soot.annotation;

public record IntElement(int value) implements Element {

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
