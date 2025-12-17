package com.asteroid.duck.velociwraptor.model.vars;

import java.util.List;

/**
 * An individual named source of values for variable substitution
 */
public enum ValueSource {
    /**
     * the `default.json` file in the template repository
     */
    TEMPLATE("template-defaults"),
    /**
     * System Properties (i.e. `-Dsome.property=some.value`)
     */
    SYSTEM_PROPERTIES("system-properties"),
    /**
     * Environment Variables
     */
    ENVIRONMENT_VARIABLES("environment-variables"),
    /**
     * JSON files supplied via the command line `-j` parameter
     */
    JSON_FILES("json-files"),
    /**
     * The `default.json` file in the velociwraptor user home directory
     */
    USER_DEFAULTS("user-defaults");

    public static final List<ValueSource> DEFAULT_ORDER =
            List.of(TEMPLATE, SYSTEM_PROPERTIES, ENVIRONMENT_VARIABLES, JSON_FILES, USER_DEFAULTS);

    private final String name;

    ValueSource(String name) {
        this.name = name;
    }
}
