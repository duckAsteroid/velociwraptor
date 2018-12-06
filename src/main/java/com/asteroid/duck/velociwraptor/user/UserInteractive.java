package com.asteroid.duck.velociwraptor.user;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.io.Closeable;
import java.io.IOException;

public interface UserInteractive extends Closeable {
    JsonValue askOption(String key, JsonArray options);
    String askFor(String key, String current);

    static UserInteractive nullInteractive() {
        return new UserInteractive() {
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
