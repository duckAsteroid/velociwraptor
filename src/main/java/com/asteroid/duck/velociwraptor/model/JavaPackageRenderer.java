package com.asteroid.duck.velociwraptor.model;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

import java.io.File;
import java.util.Locale;
import java.util.Map;

/**
 * Turns a <code>.</code> separated string into a string separated by {@link File#pathSeparator}
 */
public class JavaPackageRenderer implements NamedRenderer {
    private static final String NAME = "package";

    @Override
    public String render(Object o, String format, Locale locale, Map<String, Object> model) {
        if(o != null && o instanceof String) {
            String s = (String)o;
            return s.replace(".", File.pathSeparator);
        }
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public RenderFormatInfo getFormatInfo() {
        return null;
    }

    @Override
    public Class<?>[] getSupportedClasses() {
        return new Class[]{String.class};
    }
}
