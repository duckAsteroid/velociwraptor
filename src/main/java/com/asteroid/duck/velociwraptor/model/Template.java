package com.asteroid.duck.velociwraptor.model;

import javax.json.JsonObject;

public interface Template {
    Directory rootDirectory();
    JsonObject projectSettings();
}
