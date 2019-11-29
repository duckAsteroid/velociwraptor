package com.asteroid.duck.velociwraptor.model;

import com.asteroid.duck.velociwraptor.user.UserInteractive;
import com.asteroid.duck.velociwraptor.util.FakeJsonString;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.StringReader;

import static org.junit.Assert.*;

public class JsonTemplateDataTest {

    private static final String TEST_FILE = "test.json";

    public static final JsonObject parseString(String json)
    {
        JsonReader reader = Json.createReader(new StringReader(json));
        return reader.readObject();
    }

    public static final JsonObject parseResource(String resource)
    {
        JsonReader reader = Json.createReader(JsonTemplateDataTest.class.getResourceAsStream(resource));
        return reader.readObject();
    }

    @Test
    public void get() {
        JsonObject test = parseResource(TEST_FILE);
        JsonTemplateData subject = new JsonTemplateData(test, UserInteractive.nullInteractive());
        assertEquals(new FakeJsonString("Chris Senior"), subject.get("Author"));
        assertEquals(new FakeJsonString("6.0.1"), subject.get("GradleVersion"));
    }
}