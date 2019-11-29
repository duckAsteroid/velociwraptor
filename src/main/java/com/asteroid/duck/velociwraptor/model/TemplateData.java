package com.asteroid.duck.velociwraptor.model;

import com.asteroid.duck.velociwraptor.util.FakeJsonString;
import org.slf4j.Logger;

import javax.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.slf4j.LoggerFactory.getLogger;

public class TemplateData<K, V> implements Map<String, Object> {
    /**
     * Logger (SLF4J)
     */
    private static final Logger LOG = getLogger(TemplateData.class);
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
        Object value = null;

        if(delegate != null) {
             value = delegate.get(key);
        }

        if (value == null) {
            value = dataObject.get(key);
        }

        if (value != null && value instanceof JsonString) {
            String jsValue = ((JsonString) value).getString();
            if (jsValue.startsWith("!")) {
                jsValue = jsValue.substring(1);
                try {
                    LOG.trace("! escaped, treating as URI");
                    URI uri = new URI(jsValue);
                    LOG.trace("Requesting data from "+jsValue);
                    InputStream input = uri.toURL().openStream();
                    byte[] bytes = input.readAllBytes();
                    jsValue = new String(bytes, StandardCharsets.UTF_8);
                    LOG.trace("Received "+jsValue);
                    // is the string actually JSON?
                    JsonReader reader = Json.createReader(new StringReader(jsValue));
                    value = reader.readObject();
                    String fragment = uri.getFragment();
                    if (fragment != null)
                    {
                        JsonValue jsonValue = ((JsonObject) value).get(fragment);
                        if (jsonValue != null) {
                            value = jsonValue;
                        }
                    }
                }
                catch(URISyntaxException | IOException e)
                {
                    // maybe it's not a URL, or we can't reach it
                    LOG.debug("Unable to interpret, request or parse - returning:"+jsValue, e);
                    value = new FakeJsonString(jsValue);
                }
            }
        }

        return value;
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
