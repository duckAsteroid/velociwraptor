package com.asteroid.duck.velociwraptor.model.vars;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Properties;

public class PropertyValueSource implements ValueProvider {
    private final Properties properties;
    private final ValueSource valueSource;
    private final String debugInfo;

    public PropertyValueSource(Properties properties, ValueSource valueSource, String debugInfo) {
        this.properties = properties;
        this.valueSource = valueSource;
        this.debugInfo = debugInfo;
    }

    public static PropertyValueSource systemProperties() {
        return new PropertyValueSource(System.getProperties(), ValueSource.SYSTEM_PROPERTIES, "");
    }

    public static PropertyValueSource environmentVariables() {
        Properties envProps = new Properties();
        System.getenv().forEach(envProps::setProperty);
        return new PropertyValueSource(envProps, ValueSource.ENVIRONMENT_VARIABLES, "");
    }

    private static JsonNode from(String property) {
        return new com.fasterxml.jackson.databind.node.TextNode(property);
    }

    @Override
    public ValueSource getValueSource() {
        return valueSource;
    }

    @Override
    public boolean has(String key) {
        return properties.containsKey(key);
    }

    @Override
    public Value get(String key) {
        var propValue = properties.getProperty(key);
        if (propValue != null) {
            return new Value(valueSource, debugInfo, key, from(propValue));
        } else {
            return null;
        }
    }
}
