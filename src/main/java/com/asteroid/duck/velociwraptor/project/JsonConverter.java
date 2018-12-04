package com.asteroid.duck.velociwraptor.project;

import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

public class JsonConverter {
    public static String asString(JsonString jsonString) {
        return jsonString.getString();
    }

    public static String asString(JsonNumber jsonNumber) {
        return jsonNumber.toString();
    }

    public static Object asObject(JsonValue jsonValue) {
        if (jsonValue == null) {
            return null;
        }
        // try to get a Java value type
        switch (jsonValue.getValueType()) {
            case ARRAY:
            case OBJECT:
            default:
                return jsonValue;
            case STRING:
                return ((JsonString)jsonValue).getString();
            case NUMBER:
                return ((JsonNumber)jsonValue).bigDecimalValue();
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
            case NULL:
                return null;
        }
    }
}
