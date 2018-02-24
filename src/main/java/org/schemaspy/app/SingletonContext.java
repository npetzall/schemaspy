package org.schemaspy.app;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SingletonContext {

    private static final SingletonContext instance = new SingletonContext();

    public static SingletonContext getInstance() {
        return instance;
    }

    private final Map<String,Context> contextMap = new ConcurrentHashMap<>();

    private SingletonContext() {}

    public Context addContext(Context context) {
        if (contextMap.containsKey(context.getContextName())) {
            throw new IllegalStateException("Context already exists");
        }
        contextMap.put(context.getContextName(), context);
        return context;
    }

    public Context getContext(String name) {
        return contextMap.get(name);
    }

    public Context removeContext(String name) {
        return contextMap.remove(name);
    }

    public Context removeContext(Context context) {
        return contextMap.remove(context.getContextName());
    }
}
