package com.feysh.corax.config.community.language.soot.annotation;

import java.util.List;
import java.util.StringJoiner;

public record ArrayElement(List<Element> elements) implements Element {

    public ArrayElement(List<Element> elements) {
        this.elements = List.copyOf(elements);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",", "{", "}");
        elements.forEach(e -> sj.add(e.toString()));
        return sj.toString();
    }
}
