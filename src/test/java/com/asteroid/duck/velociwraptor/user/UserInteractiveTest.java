package com.asteroid.duck.velociwraptor.user;

import org.junit.Test;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

import static org.junit.Assert.*;

public class UserInteractiveTest {

    @Test
    public void nullInteractive() {
        UserInteractive subject = UserInteractive.nullInteractive();

        JsonValue optionValue = subject.askOption("key", Json.createArrayBuilder().add(1).add(2).add(3).build());
        assertEquals(JsonValue.ValueType.NUMBER, optionValue.getValueType());
        assertEquals(1, ((JsonNumber)optionValue).intValue());

        String value = subject.askFor("key2", "wibble");
        assertEquals("wibble", value);
    }
}