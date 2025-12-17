package com.asteroid.duck.velociwraptor.model.vars;

/**
 * Interface to an object that can provide values to the {@link TemplateDataModel}
 */
public interface ValueProvider {
    /**
     * What kind of value source is this
     */
    ValueSource getValueSource();

    /**
     * Does this provider have a value for the given key
     * @param key the variable name
     * @return true if it has a value for the key
     */
    boolean has(String key);

    /**
     * Get the value for the given key
     * @param key the variable name
     * @return the value, or null if not found
     */
    Value get(String key);
}
