package com.asteroid.duck.velociwraptor.model.vars;

import com.asteroid.duck.velociwraptor.user.UserInteractive;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TemplateDataModel extends AbstractMap<String, Object> {
    private final static Logger log = LoggerFactory.getLogger(TemplateDataModel.class);
    /**
     * A cache of all the values in this data model, mapped by their keys.
     * Values are added on demand when requested for the first time.
     */
    private final Map<String, Object> valueCache = new HashMap<>();
    /**
     * A list of all value providers that supply values for this data model
     */
    private final List<ValueProvider> valueProviders;
    /**
     * A user interactivity handler to resolve values interactively
     */
    private final UserInteractive userInteractive;

    public TemplateDataModel(List<ValueProvider> valueProviders, UserInteractive userInteractive) {
        this.valueProviders = Objects.requireNonNull(valueProviders);
        this.userInteractive = Objects.requireNonNull(userInteractive);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return valueCache.entrySet();
    }

    private Object resolve(String key) {
        var values = valueProviders.stream()
                // only ask providers that support the key
                .filter(provider -> provider.has(key))
                // get the value for the key
                .map(provider -> provider.get(key))
                .toList();
        var resolved = userInteractive.resolve(key, values).map(Value::asJavaObject);
        if (log.isDebugEnabled()) {
            log.debug("Resolved key '{}' to {} from value(s): {}", key, resolved.orElse(null), values.stream().map(Objects::toString).collect(Collectors.joining(",","[","]")));
        }
        return resolved.orElse(null);
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String k)) return null;
        // Only attempt to load if the key is known
        if (!valueCache.containsKey(k)) {
            Object v = resolve(k);
            // store even nulls to mark "loaded"
            valueCache.put(k, v);
        }
        return valueCache.get(k);
    }

}
