package com.asteroid.duck.velociwraptor.model;


import com.fasterxml.jackson.databind.JsonNode;

public interface ValueAdapter {
    Object get(String key, JsonNode current);
}
