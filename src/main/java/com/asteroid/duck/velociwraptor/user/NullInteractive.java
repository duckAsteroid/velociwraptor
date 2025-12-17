package com.asteroid.duck.velociwraptor.user;

import com.asteroid.duck.velociwraptor.model.vars.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class NullInteractive implements UserInteractive {

    @Override
    public Optional<Value> resolve(String key, Stream<Value> values) {
        return values.findFirst();
    }

    @Override
    public void close() throws Exception {
        // nothing to do
    }
}
