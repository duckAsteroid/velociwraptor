package com.asteroid.duck.velociwraptor.template;

public class TemplateSection {
    /**
     * Is this section a template section or a straight copy section
     */
    private final boolean template;
    /**
     * The content of this section
     */
    private final String content;

    public TemplateSection(boolean template, String content) {
        this.template = template;
        this.content = content;
    }

    public boolean isTemplate() {
        return template;
    }

    public String content() {
        return content;
    }
}
