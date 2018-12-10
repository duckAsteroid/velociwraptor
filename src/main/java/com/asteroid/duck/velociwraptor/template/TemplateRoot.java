package com.asteroid.duck.velociwraptor.template;

import javax.json.JsonObject;

public interface TemplateRoot {
    Directory rootDirectory();
    JsonObject projectSettings();
}
