package com.asteroid.duck.velociwraptor.template;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * A template folder within the template directory
 */
public interface Directory {
    String rawName();
    Stream<File> childFiles() throws IOException;
    Stream<Directory> childDirs() throws IOException;
}
