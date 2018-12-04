package com.asteroid.duck.velociwraptor.project;

import org.apache.velocity.context.AbstractContext;

import javax.json.JsonArray;

public class CommandLineProvider extends AbstractContext {
    private final UserInteractive userInteractive;
    private final JsonContext delegate;

    public CommandLineProvider(UserInteractive userInteractive, JsonContext delegate) {
        this.userInteractive = userInteractive;
        this.delegate = delegate;
    }

    @Override
    public Object internalGet(String key) {
        Object fileObject = null;
        if (delegate != null) {
            fileObject = delegate.get(key);
        }
        if (fileObject == null || fileObject instanceof String) {
            return userInteractive.askFor(key, (String)fileObject);
        }
        else if (fileObject instanceof JsonArray) {
            JsonArray array = (JsonArray)fileObject;
            return userInteractive.askOption(key, array);
        }
        return fileObject;
    }



    @Override
    public Object internalPut(String key, Object value) {
        return delegate.internalPut(key, value);
    }

    @Override
    public boolean internalContainsKey(String key) {
        return delegate.containsKey(key);
    }

    @Override
    public String[] internalGetKeys() {
        return delegate.internalGetKeys();
    }

    @Override
    public Object internalRemove(String key) {
        return delegate.internalRemove(key);
    }
}
