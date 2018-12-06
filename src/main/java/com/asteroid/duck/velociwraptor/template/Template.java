package com.asteroid.duck.velociwraptor.template;

import javax.json.JsonObject;

public interface Template {
    Directory rootDirectory();
    JsonObject projectSettings();
}
