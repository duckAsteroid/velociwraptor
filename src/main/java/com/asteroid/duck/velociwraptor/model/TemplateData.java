package com.asteroid.duck.velociwraptor.model;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class TemplateData<K, V> implements Map<String, Object> {

    /**
     * A delegate who gets a chance to provide a value if this cannot
     */
    protected final TemplateData delegate;
    /**
     * An internal data storage object to pass calls onto
     */
    protected final Map<K,V> dataObject;
    /**
     * Transforms keys in the data storage to String keys
     */
    protected final Function<K, String> keyConverter;

    public TemplateData(TemplateData delegate, Map<K, V> dataObject, Function<K, String> keyConverter) {
        this.delegate = delegate;
        this.dataObject = dataObject;
        this.keyConverter = keyConverter;
    }

    public static TemplateData<String, String> systemEnvironment(TemplateData delegate) {
        return new TemplateData(delegate, System.getenv(), identity());
    }

    public static TemplateData<Object, Object> systemProperties(TemplateData delegate) {
        return new TemplateData(delegate, System.getProperties(), Object::toString);
    }

    public static TemplateData<String, Object> wrap(Map<String, Object> data) {
        return new TemplateData<>(null, data, Objects::toString);
    }

    @Override
    public Object get(Object key) {
        Object delegateValue = null;

        if(delegate != null) {
             delegateValue = delegate.get(key);
        }

        if (delegateValue == null) {
            return dataObject.get(key);
        }

        return delegateValue;
    }

    @Override
    public int size() {
        return dataObject.size();
    }

    @Override
    public boolean isEmpty() {
        return dataObject.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return dataObject.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return dataObject.containsValue(value);
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(Object key) {
        return dataObject.remove(key);
    }

    @Override
    public void clear() {
        dataObject.clear();
    }

    @Override
    public Set<String> keySet() {
        return dataObject.keySet().stream().map(keyConverter).collect(Collectors.toSet());
    }

    @Override
    public Collection<Object> values() {
        return new ArrayList<>(dataObject.values());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        HashSet<Entry<String, Object>> result = new HashSet<>();
        for(Entry<K, V> entry : dataObject.entrySet()) {
            result.add(new SimpleEntry(keyConverter.apply(entry.getKey()), entry.getValue()));
        }
        return result;
    }

    protected static class SimpleEntry implements Entry<String, Object> {
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
}
