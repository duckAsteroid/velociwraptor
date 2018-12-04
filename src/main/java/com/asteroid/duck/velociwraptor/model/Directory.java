package com.asteroid.duck.velociwraptor.model;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * A template folder within the template directory
 */
public interface Directory {
    String rawName();
    Stream<File> childFiles() throws IOException;
    Stream<Directory> childDirs() throws IOException;
}
