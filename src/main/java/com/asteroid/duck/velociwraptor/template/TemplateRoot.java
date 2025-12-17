package com.asteroid.duck.velociwraptor.template;

import com.asteroid.duck.velociwraptor.model.vars.ValueProvider;

import java.util.stream.Stream;

/**
 * Represents the root of a template structure, providing access to the root directory of the template itself
 * and any project settings.
 */
public interface TemplateRoot extends TemplateNode, AutoCloseable {
    default String rawName() {
        return "";
    }

    @Override
    default Stream<TemplateNode> childNodes() {
        return Stream.of(rootDirectory());
    }

    /**
     * Gets the root directory of the template.
     *
     * @return the root directory
     */
    TemplateDirectory rootDirectory();
    /**
     * Gets the project settings value provider.
     *
     * @return the project settings
     */
    ValueProvider projectSettings();
}
