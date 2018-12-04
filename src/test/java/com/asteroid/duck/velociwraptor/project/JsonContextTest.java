package com.asteroid.duck.velociwraptor.project;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import static org.junit.Assert.*;

public class JsonContextTest {

    private JsonContext subject;

    @Before
    public void setUp() throws Exception {
        InputStream stream = JsonContextTest.class.getResourceAsStream("test.json");
        InputStreamReader streamReader = new InputStreamReader(stream);
        JsonReader reader = Json.createReader(streamReader);
        JsonObject jsonObject = reader.readObject();
        subject = new JsonContext(jsonObject);
        streamReader.close();
    }

    @After
    public void tearDown() throws Exception {
        subject = null;
    }

    @Test
    public void testReadJsonContext() {
        assertEquals("example-project", subject.get("Name"));
        assertEquals(new BigDecimal(2018), subject.get("Year"));
        assertEquals(3.14, ((BigDecimal)subject.get("Fraction")).doubleValue(), 0.001);
        assertEquals("Chris Senior", subject.get("Author"));
        Object licence = subject.get("License");
        assertNotNull(licence);
        assertTrue(licence instanceof JsonArray);
        JsonArray licences = (JsonArray) licence;
        assertEquals(3, licences.size());

        assertNull(subject.get("wibble"));
    }

    @Test
    public void testWriteJsonContext() {
        assertFalse(subject.containsKey("Test"));
        subject.put("Test", "New value");
        assertTrue(subject.containsKey("Test"));
        assertEquals("New value", subject.get("Test"));
        subject.remove("Test");
        assertFalse(subject.containsKey("Test"));
    }
}