package com.asteroid.duck.velociwraptor.model;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class TemplateDataTest {

    @Test
    public void systemEnvironment() {
        // no delegate
        TemplateData<String, String> environment = TemplateData.systemEnvironment(null);
        Map<String, String> systemEnv = System.getenv();
        for(Map.Entry<String, String> env : systemEnv.entrySet()) {
            assertTrue(environment.containsKey(env.getKey()));
            assertEquals(env.getValue(), environment.get(env.getKey()));
        }

        final String KEY = "Test-Secret-Key-Velociwraptor";
        final Object VALUE = new Integer(1664);
        Map<String, Object> delegateData = Collections.singletonMap(KEY, VALUE);
        TemplateData<String, Object> delegate = TemplateData.wrap(delegateData);
        environment = TemplateData.systemEnvironment(delegate);
        assertEquals(VALUE, environment.get(KEY));
    }

    @Test
    public void systemProperties() {
    }
}