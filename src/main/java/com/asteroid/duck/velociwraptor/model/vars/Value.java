package com.asteroid.duck.velociwraptor.model.vars;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A value with its source and debug info
 * @param source the source of the value
 * @param debugInfo extra debug info about the source of the value (e.g. the file it came from)
 * @param key the variable name (key) for the value
 * @param value the actual value
 */
public record Value(ValueSource source, String debugInfo, String key, JsonNode value) {
    public Value(ValueSource source, String debugInfo, String key, JsonNode value) {
        this.source = Objects.requireNonNull(source);
        this.debugInfo = debugInfo;
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
        if (value.isMissingNode()) throw new IllegalArgumentException("Cannot create Value with 'missing' node");
        if (value.isObject()) throw new IllegalArgumentException("Cannot create Value with 'object' node");
    }

    public boolean isArray() {
        return value.isArray();
    }

    public Stream<Value> asArray() {
        if (!isArray()) return Stream.of(this);
        IntStream range = IntStream.range(0, value.size());
        return range.mapToObj(i -> new Value(source, childDebugInfo(debugInfo, i), key, value.get(i)));
    }

    private static String childDebugInfo(String debugInfo, int index) {
        return (debugInfo == null ? "" : debugInfo + " ") + "[" + index + "]";
    }

    /**
     * Convert this Value to a native Java object
     * @return the Java object
     */
    public Object asJavaObject() {
        return asJavaObject(value);
    }

    /**
     * Convert a JsonNode to a native Java object
     * @param node the JsonNode
     * @return the Java object
     */
    public static Object asJavaObject(JsonNode node) {
        if (node.isArray()) {
            return StreamSupport.stream(Spliterators.spliterator(node.elements(), node.size(), Spliterator.ORDERED), false)
                    .map(Value::asJavaObject)
                    .toList();
        }
        else if (node.isTextual()) {
            return node.asText();
        } else if (node.isInt()) {
            return node.asInt();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else if (node.isFloat()) {
            return (float)node.asDouble();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isBinary()) {
            try {
                return node.binaryValue();
            }
            catch(IOException ioe) {
                throw new IllegalStateException("Unable to get binary value from JsonNode", ioe);
            }
        } else if (node.isNull()) {
            return null;
        } else if(node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                map.put(entry.getKey(), asJavaObject(entry.getValue()));
            }
            return map;
        } else {
            throw new IllegalStateException("Unsupported JsonNode type for conversion to Java object: " + node.getNodeType());
        }
    }
}
