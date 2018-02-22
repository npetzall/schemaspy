package org.schemaspy.app;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonContext {

    private static final SingletonContext instance = new SingletonContext();

    public static SingletonContext getInstance() {
        return instance;
    }

    private final Map<String,Context> contextMap = new ConcurrentHashMap<>();

    private SingletonContext() {}

    public Context addContext(String name, Context context) {
        if (contextMap.containsKey(name)) {
            throw new IllegalStateException("Context already exists");
        }
        contextMap.put(name, context);
        return context;
    }

    public Context getContext(String name) {
        return contextMap.get(name);
    }

    public Context removeContext(String name) {
        return contextMap.remove(name);
    }
}
