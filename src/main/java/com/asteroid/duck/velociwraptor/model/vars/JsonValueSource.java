package com.asteroid.duck.velociwraptor.model.vars;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class JsonValueSource implements ValueProvider {
    private static final Logger log = LoggerFactory.getLogger(JsonValueSource.class);
    private final String jsonPath;
    private final JsonNode jsonObject;
    private final ValueSource valueSource;
    private static final ObjectMapper jsonObjectMapper = new ObjectMapper();

    public JsonValueSource(ValueSource valueSource, String jsonPath, JsonNode jsonObject) {
        this.valueSource = valueSource;
        this.jsonPath = jsonPath;
        this.jsonObject = jsonObject;
    }

    public static JsonValueSource fromFile(ValueSource valueSource, File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try {
            JsonNode jsonNode = jsonObjectMapper.readTree(file);
            return new JsonValueSource(valueSource, file.getAbsolutePath(), jsonNode);
        } catch (IOException e) {
            log.error("Error reading file " + file.getAbsolutePath(), e);
            return null;
        }
    }

    @Override
    public ValueSource getValueSource() {
        return valueSource;
    }

    @Override
    public boolean has(String key) {
        return jsonObject.has(key);
    }

    @Override
    public Value get(String key) {
        return new Value(valueSource, jsonPath, key, jsonObject.get(key));
    }
}
