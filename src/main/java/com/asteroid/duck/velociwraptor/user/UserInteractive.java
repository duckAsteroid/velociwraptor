package com.asteroid.duck.velociwraptor.user;

import com.asteroid.duck.velociwraptor.model.JsonConverter;
import com.asteroid.duck.velociwraptor.model.ValueAdapter;

import javax.json.*;
import java.io.Closeable;
import java.io.IOException;

public abstract class UserInteractive implements ValueAdapter, Closeable {
    public abstract JsonValue askOption(String key, JsonArray options);
    public abstract Boolean askBooleanOption(String key, boolean current);
    public abstract String askFor(String key, String current);

    @Override
    public Object get(String promptKey, JsonValue jsonValue) {
        switch (jsonValue.getValueType()) {
            case ARRAY :
                return askOption(promptKey, (JsonArray)jsonValue);

            case NUMBER:
                String numString = JsonConverter.asString((JsonNumber)jsonValue);
                return asJsonString(askFor(promptKey, numString));

            case STRING:
                return asJsonString(askFor(promptKey, JsonConverter.asString((JsonString)jsonValue)));

            case TRUE:
                return askBooleanOption(promptKey, true);

            case FALSE:
                return askBooleanOption(promptKey, false);

        }
        return jsonValue;
    }

    private JsonValue asJsonString(String value) {
        return Json.createArrayBuilder().add(value).build().get(0);
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
