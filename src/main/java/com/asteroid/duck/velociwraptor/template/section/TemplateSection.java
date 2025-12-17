package com.asteroid.duck.velociwraptor.template.section;

import com.asteroid.duck.velociwraptor.template.TemplateNode;
import com.asteroid.duck.velociwraptor.template.visit.TemplateNodeVisitor;

import java.util.stream.Stream;

/**
 * @param template Is this section a template section or a straight copy section
 * @param content  The content of this section
 */
public record TemplateSection(boolean template, String content) implements TemplateNode {
    @Override
    public String rawName() {
        return "section";
    }

    @Override
    public Stream<? extends TemplateNode> childNodes() {
        return Stream.empty();
    }

    @Override
    public void accept(TemplateNodeVisitor visitor) {
        if (visitor != null) {
            visitor.visitSection(this);
        }
    }
}
