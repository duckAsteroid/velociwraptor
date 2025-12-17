package com.asteroid.duck.velociwraptor.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.floreysoft.jmte.Renderer;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonConverter implements Renderer<JsonNode> {

    public static Stream<JsonNode> stream(JsonNode node) {
        if (node.isArray()) {
            return Stream.of(node).flatMap(n -> {
                Stream<JsonNode> s = Stream.empty();
                for (JsonNode item : n) {
                    s = Stream.concat(s, stream(item));
                }
                return s;
            });
        } else {
            return Stream.empty();
        }
    }

    @Override
    public String render(JsonNode o, Locale locale, Map<String, Object> model) {
        return switch (o.getNodeType()) {
            case STRING -> o.asText();
            case NUMBER -> o.numberValue().toString();
            case BOOLEAN -> Boolean.toString(o.asBoolean());
            case NULL -> "null";
            case MISSING -> "?";
            default -> o.toString();
        };
    }
}
