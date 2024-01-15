package com.feysh.corax.config.community.language.soot.annotation;

public record StringElement(String value) implements Element {

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
