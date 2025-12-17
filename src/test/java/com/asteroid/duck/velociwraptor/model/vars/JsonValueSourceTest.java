package com.asteroid.duck.velociwraptor.model.vars;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;

import java.io.IOException;

import static com.asteroid.duck.velociwraptor.AssertFile.assertJsonString;

public class JsonValueSourceTest extends TestCase {

    private static final String TEST_FILE = "test.json";
    private static ObjectMapper mapper = new ObjectMapper();

    public static final JsonNode parseString(String json) throws JsonProcessingException {
        return mapper.readTree(json);
    }

    public static final JsonNode parseResource(String resource) throws IOException {
        return mapper.readTree(JsonValueSourceTest.class.getResourceAsStream(resource));
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGet() throws IOException {
        JsonNode test = parseResource(TEST_FILE);
        JsonValueSource subject = new JsonValueSource(
                ValueSource.JSON_FILES,
                TEST_FILE,
                test
        );
        var value = subject.get("Author");
        assertJsonString("Chris Senior", value.value());
    }
}