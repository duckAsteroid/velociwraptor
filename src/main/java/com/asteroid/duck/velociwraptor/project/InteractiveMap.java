package com.asteroid.duck.velociwraptor.project;

import com.asteroid.duck.velociwraptor.user.UserInteractive;

import javax.json.*;
import java.util.*;

public class InteractiveMap implements Map<String, Object> {
    private final JsonObject baseObject;
    private final UserInteractive user;
    private final String prefix;

    public InteractiveMap(JsonObject baseObject, UserInteractive user) {
        this(null, baseObject, user);
    }

    public InteractiveMap(String prefix, JsonObject baseObject, UserInteractive user) {
        this.prefix = prefix;
        this.baseObject = baseObject;
        this.user = user == null ? UserInteractive.nullInteractive() : user;
    }

    @Override
    public int size() {
        return baseObject.size();
    }

    @Override
    public boolean isEmpty() {
        return baseObject.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return baseObject.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return baseObject.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        JsonValue jsonValue = baseObject.get(key);
        if (jsonValue != null && user != null) {
            String promptKey = prefix() + key;
            switch (jsonValue.getValueType()) {
                case ARRAY :
                    return user.askOption(promptKey, (JsonArray)jsonValue);

                case NUMBER:
                    String numString = JsonConverter.asString((JsonNumber)jsonValue);
                    return asJsonString(user.askFor(promptKey, numString));

                case STRING:
                    return asJsonString(user.askFor(promptKey, JsonConverter.asString((JsonString)jsonValue)));

                case TRUE:
                    return user.askOption(promptKey, Json.createArrayBuilder().add("true").add("false").build());

                case FALSE:
                    return user.askOption(promptKey, Json.createArrayBuilder().add("false").add("true").build());

                case OBJECT:
                    return new InteractiveMap(promptKey, (JsonObject)jsonValue, user);
            }
        }
        return jsonValue;
    }

    private JsonValue asJsonString(String value) {
        return Json.createArrayBuilder().add(value).build().get(0);
    }

    private String prefix() {
        if (prefix == null || prefix.length() <=0) {
            return "";
        }
        else {
            return prefix + ".";
        }
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(Object key) {
        return baseObject.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        baseObject.clear();
    }

    @Override
    public Set<String> keySet() {
        return baseObject.keySet();
    }

    @Override
    public Collection<Object> values() {
        return new ArrayList<>(baseObject.values());
    }

    private static class SimpleEntry implements Entry<String, Object> {
        private final String key;
        private final Object value;

        private SimpleEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        HashSet<Entry<String, Object>> result = new HashSet<>();
        for(Entry<String, JsonValue> entry : baseObject.entrySet()) {
            result.add(new SimpleEntry(entry.getKey(), entry.getValue()));
        }
        return result;
    }

}
