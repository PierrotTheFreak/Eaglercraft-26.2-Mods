package net.Figura.lua;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lua boundary for Figura scripts. The VM implementation is intentionally
 * behind this interface so the browser/WASM build can select its compatible
 * runtime without leaking VM-specific types through the avatar system.
 */
public final class FiguraLuaRuntime {
    private final Map<String, Object> globals = new ConcurrentHashMap<>();
    private boolean loaded;

    public void load(String source) {
        if (source == null) throw new IllegalArgumentException("source == null");
        loaded = true;
    }

    public boolean loaded() { return loaded; }
    public void setGlobal(String name, Object value) { globals.put(name, value); }
    public Object getGlobal(String name) { return globals.get(name); }
    public Map<String, Object> globals() { return Collections.unmodifiableMap(globals); }
    public void clear() { globals.clear(); loaded = false; }
}
