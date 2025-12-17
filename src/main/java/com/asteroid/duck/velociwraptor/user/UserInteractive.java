package com.asteroid.duck.velociwraptor.user;

import com.asteroid.duck.velociwraptor.model.vars.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface to object that might handle value resolution interactivity with the user
 */
public interface UserInteractive extends AutoCloseable {
    /**
     * Ask the user to provide a value for the given variable (key).
     * This might be from one of the given values, or could be a new value, or null...
     * @param key the variable name (key) to resolve
     * @param values the current values for the key
     * @return the resolved value provided by the user(if any)
     */
    default Optional<Value> resolve(String key, List<Value> values) {
        return resolve(key, flattenValues(values));
    }

    Optional<Value> resolve(String key, Stream<Value> values);
    /**
     * Flatten a list of values, expanding any array values into their individual elements
     * @param values the list of values to flatten
     * @return a stream of flattened values
     */
    static Stream<Value> flattenValues(List<Value> values) {
        return values.stream().flatMap(Value::asArray);
    }
}
