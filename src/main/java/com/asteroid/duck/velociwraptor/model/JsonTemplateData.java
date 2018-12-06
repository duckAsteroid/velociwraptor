package com.asteroid.duck.velociwraptor.model;

import javax.json.*;
import java.nio.file.Path;

/**
 * Provides the Map view of some JSON data as required by the template engine.
 * Also calls out to a value adapter to have the values in the map "meddled with" (e.g. interactive user prompts)
 */
public class JsonTemplateData extends TemplateData<String, JsonValue> {
    /**
     * A key name prefix used when talking to the value adapter (e.g. human)
     */
    private final String prefix;
    /**
     * An adapter that changes
     */
    private final ValueAdapter valueAdapter;

    public JsonTemplateData(JsonObject json, ValueAdapter valueAdapter) {
        this(null, null, json, valueAdapter);
    }

    public JsonTemplateData(String prefix, TemplateData delegate, JsonObject json, ValueAdapter valueAdapter) {
        super(delegate, json, o -> o.toString());
        this.prefix = prefix;
        if (valueAdapter == null) {
            throw new IllegalArgumentException("Value adapter cannot be null");
        }
        this.valueAdapter = valueAdapter;
    }

    protected String prefix() {
        if (prefix == null || prefix.length() <=0) {
            return "";
        }
        else {
            return prefix + ".";
        }
    }

    @Override
    public Object get(Object key) {
        JsonValue jsonValue = dataObject.get(key);
        // we return objects..
        Object result = jsonValue;

        // ask adapter for another value...
        String promptKey = prefix() + key;
        if (jsonValue != null) {
            result = valueAdapter.get(promptKey, jsonValue);
        }

        // wrap the any JSON object in a partial cloned Map (supports property chaining)
        if(result instanceof JsonObject) {
            result = new JsonTemplateData(promptKey, delegate, (JsonObject)jsonValue, valueAdapter);
        }
        return result;
    }

}
