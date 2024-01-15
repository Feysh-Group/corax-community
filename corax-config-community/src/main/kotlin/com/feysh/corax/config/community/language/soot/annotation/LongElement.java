package com.feysh.corax.config.community.language.soot.annotation;

public record LongElement(long value) implements Element {

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
