package net.Figura.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joml.Vector3f;

/** Mutable Figura model node independent of Minecraft's 26.2 model classes. */
public final class FiguraModelPart {
    private final String name;
    private final List<FiguraModelPart> children = new ArrayList<>();
    private final Vector3f position = new Vector3f();
    private final Vector3f rotation = new Vector3f();
    private final Vector3f scale = new Vector3f(1f, 1f, 1f);
    private boolean visible = true;

    public FiguraModelPart(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("blank model part name");
        this.name = name;
    }

    public String name() { return name; }
    public List<FiguraModelPart> children() { return Collections.unmodifiableList(children); }
    public Vector3f position() { return position; }
    public Vector3f rotation() { return rotation; }
    public Vector3f scale() { return scale; }
    public boolean visible() { return visible; }
    public void visible(boolean value) { visible = value; }
    public FiguraModelPart addChild(FiguraModelPart child) { children.add(child); return child; }
}
