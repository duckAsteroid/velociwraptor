package com.asteroid.duck.velociwraptor.user;

import com.asteroid.duck.velociwraptor.model.JsonConverter;
import com.asteroid.duck.velociwraptor.model.ValueAdapter;
import com.asteroid.duck.velociwraptor.util.FakeJsonString;

import javax.json.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class UserInteractive implements ValueAdapter, Closeable {
    public abstract JsonValue askOption(String key, JsonArray options);
    public abstract Boolean askBooleanOption(String key, boolean current);
    public abstract String askFor(String key, String current);
    private Map<String, Object> cache = new HashMap<>();

    @Override
    public Object get(String promptKey, JsonValue jsonValue) {
        if (!cache.containsKey(promptKey)) {
            Object result = jsonValue;
            switch (jsonValue.getValueType()) {
                case ARRAY:
                    result = askOption(promptKey, (JsonArray) jsonValue);
                    break;

                case NUMBER:
                    String numString = JsonConverter.asString((JsonNumber) jsonValue);
                    result = asJsonString(askFor(promptKey, numString));
                    break;

                case STRING:
                    result = asJsonString(askFor(promptKey, JsonConverter.asString((JsonString) jsonValue)));
                    break;

                case TRUE:
                    result = askBooleanOption(promptKey, true);
                    break;

                case FALSE:
                    result = askBooleanOption(promptKey, false);
                    break;

            }
            cache.put(promptKey, result);
            return result;
        }
        return cache.get(promptKey);
    }

    private JsonValue asJsonString(String value) {
        return new FakeJsonString(value);
    }


    public static UserInteractive nullInteractive() {
        return new UserInteractive() {
            @Override
            public Boolean askBooleanOption(String key, boolean current) {
                return current;
            }

            @Override
            public JsonValue askOption(String key, JsonArray options) {
                return options.get(0);
            }

            @Override
            public String askFor(String key, String current) {
                return current;
            }

            @Override
            public void close() throws IOException {
                //no-op
            }
        };
    }
}
