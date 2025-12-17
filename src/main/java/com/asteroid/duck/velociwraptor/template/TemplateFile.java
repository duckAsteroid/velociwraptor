package com.asteroid.duck.velociwraptor.template;

import com.asteroid.duck.velociwraptor.template.section.TemplateSection;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * A template file within a template
 */
public interface TemplateFile extends TemplateNode {
    InputStream rawContent() throws IOException;
}
