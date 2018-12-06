package com.asteroid.duck.velociwraptor.template;

import java.io.IOException;
import java.io.InputStream;

/**
 * A template file within a template
 */
public interface File {
    String rawName();
    InputStream rawContent() throws IOException;
}
