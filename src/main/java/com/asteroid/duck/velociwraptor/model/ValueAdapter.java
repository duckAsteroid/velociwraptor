package com.asteroid.duck.velociwraptor.model;

import javax.json.JsonValue;

public interface ValueAdapter {
    Object get(String key, JsonValue current);
}
