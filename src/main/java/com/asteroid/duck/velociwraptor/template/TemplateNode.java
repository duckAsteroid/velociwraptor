package com.asteroid.duck.velociwraptor.template;

import com.asteroid.duck.velociwraptor.template.visit.TemplateNodeVisitor;

import java.util.stream.Stream;

public interface TemplateNode {
    String rawName();

    Stream<? extends TemplateNode> childNodes();
    /**
     * Visit this node recursively with the given visitor
     * @param visitor the visitor
     */
    void accept(TemplateNodeVisitor visitor);
}
