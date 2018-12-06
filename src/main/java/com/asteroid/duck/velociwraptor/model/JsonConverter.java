package com.asteroid.duck.velociwraptor.model;

import com.floreysoft.jmte.Renderer;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Locale;
import java.util.Map;

public class JsonConverter implements Renderer<JsonValue> {

    public static String asString(JsonString jsonString) {
        return jsonString.getString();
    }

    public static String asString(JsonNumber jsonNumber) {
        return jsonNumber.toString();
    }

    @Override
    public String render(JsonValue o, Locale locale, Map<String, Object> model) {
        switch (o.getValueType()) {
            case ARRAY:
                JsonArray array = (JsonArray)o;
                array.getValuesAs(JsonValue.class).stream().forEach(jsonValue -> render(jsonValue, locale, model));
                break;
            case OBJECT:
                return o.toString();

            case STRING:
                return ((JsonString)o).getString();

            case NUMBER:
                return ((JsonNumber)o).bigDecimalValue().toPlainString();

            case TRUE:
                return "true";

            case FALSE:
                return "false";

            case NULL:
                return "null";
        }
        return o.toString();
    }
}
