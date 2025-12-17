package com.asteroid.duck.velociwraptor.template.visit;

import com.asteroid.duck.velociwraptor.template.TemplateDirectory;
import com.asteroid.duck.velociwraptor.template.TemplateFile;
import com.asteroid.duck.velociwraptor.template.TemplateRoot;
import com.asteroid.duck.velociwraptor.template.section.TemplateSection;

public interface TemplateNodeVisitor {
    void visitRoot(TemplateRoot root);
    void visitDirectory(TemplateDirectory directory);
    void visitFile(TemplateFile file);
    void visitSection(TemplateSection section);
}
