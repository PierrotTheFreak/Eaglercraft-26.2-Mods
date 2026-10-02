package net.Figura.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Figura's version-neutral model graph. The 26.2 renderer consumes this graph. */
public final class FiguraModel {
    private final FiguraModelPart root = new FiguraModelPart("root");
    private final Map<String, FiguraModelPart> parts = new ConcurrentHashMap<>();

    public FiguraModel() { parts.put(root.name(), root); }
    public FiguraModelPart root() { return root; }

    public FiguraModelPart createPart(String name, String parent) {
        if (parts.containsKey(name)) throw new IllegalArgumentException("duplicate model part: " + name);
        FiguraModelPart parentPart = parts.get(parent);
        if (parentPart == null) throw new IllegalArgumentException("unknown parent: " + parent);
        FiguraModelPart part = new FiguraModelPart(name);
        parentPart.addChild(part);
        parts.put(name, part);
        return part;
    }

    public FiguraModelPart part(String name) { return parts.get(name); }
    public Map<String, FiguraModelPart> parts() { return Map.copyOf(parts); }

    public void tick(int tick) {
        // Animation evaluation is deliberately separated from rendering.
        // The animation system mutates this graph before the 26.2 renderer visits it.
    }
}
