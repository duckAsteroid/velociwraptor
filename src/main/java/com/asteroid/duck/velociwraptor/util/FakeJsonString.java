package com.asteroid.duck.velociwraptor.util;

import javax.json.JsonString;
import java.util.Objects;

public class FakeJsonString implements JsonString {
    private final String string;

    public FakeJsonString(String string) {
        this.string = string;
    }

    @Override
    public String getString() {
        return string;
    }

    @Override
    public CharSequence getChars() {
        return string;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.STRING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof JsonString)) return false;
        JsonString that = (JsonString) o;
        return Objects.equals(string, that.getString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }

    @Override
    public String toString() {
        return string;
    }
}
