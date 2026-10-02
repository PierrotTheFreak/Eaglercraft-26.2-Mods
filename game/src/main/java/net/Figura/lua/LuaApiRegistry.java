package net.Figura.lua;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Small host-function registry used by the eventual Cobalt/WASM Lua adapter. */
public final class LuaApiRegistry {
    private final Map<String, Function<Object[], Object>> functions = new ConcurrentHashMap<>();

    public void register(String name, Function<Object[], Object> function) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("blank API name");
        functions.put(name, function);
    }

    public Object call(String name, Object... args) {
        Function<Object[], Object> function = functions.get(name);
        if (function == null) throw new IllegalArgumentException("Unknown Figura API: " + name);
        return function.apply(args);
    }

    public boolean contains(String name) { return functions.containsKey(name); }
    public int size() { return functions.size(); }
}
