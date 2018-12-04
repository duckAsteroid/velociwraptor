package com.asteroid.duck.velociwraptor.project;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.AbstractContext;

import javax.json.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.asteroid.duck.velociwraptor.project.JsonConverter.asObject;

/**
 * A velocity context that wraps a JSON object.
 * When written to the values are stored in a local Map cache.
 * If the cache contains a value, it will be returned rather than a value from the JSON.
 */
public class JsonContext extends AbstractContext {
    private final JsonObject project;
    private final Map<String, Object> localValues = new HashMap<>();

    public JsonContext(JsonObject project) {
        this.project = project;
    }

    @Override
    public Object internalGet(String key) {
        if (localValues.containsKey(key)) {
            return localValues.get(key);
        }
        JsonValue jsonValue = project.get(key);
        return asObject(jsonValue);
    }

    @Override
    public Object internalPut(String key, Object value) {
        return localValues.put(key, value);
    }

    @Override
    public boolean internalContainsKey(String key) {
        return localValues.containsKey(key) || project.containsKey(key);
    }

    @Override
    public String[] internalGetKeys() {
        TreeSet<String> result = new TreeSet<>();
        result.addAll(localValues.keySet());
        result.addAll(project.keySet());
        return result.stream().collect(Collectors.toList()).toArray(new String[result.size()]);
    }

    @Override
    public Object internalRemove(String key) {
        return localValues.remove(key);
    }
}
