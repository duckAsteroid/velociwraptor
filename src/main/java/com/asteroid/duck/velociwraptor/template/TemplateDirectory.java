package com.asteroid.duck.velociwraptor.template;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * A template folder within the template directory
 */
public interface TemplateDirectory extends TemplateNode {
    Stream<TemplateFile> childFiles();
    Stream<TemplateDirectory> childDirs();

    @Override
    default Stream<TemplateNode> childNodes() {
        return Stream.concat(childFiles(), childDirs());
    }
}
