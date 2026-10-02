package net.Figura.model;

import org.joml.Vector2f;
import org.joml.Vector3f;

/** A textured Blockbench cube in Figura's model graph. */
public final class BlockbenchCube {
    private final Vector3f origin = new Vector3f();
    private final Vector3f size = new Vector3f();
    private final Vector3f rotation = new Vector3f();
    private final Vector2f uv = new Vector2f();
    private String texture;
    private boolean mirror;

    public Vector3f origin() { return origin; }
    public Vector3f size() { return size; }
    public Vector3f rotation() { return rotation; }
    public Vector2f uv() { return uv; }
    public String texture() { return texture; }
    public void texture(String texture) { this.texture = texture; }
    public boolean mirror() { return mirror; }
    public void mirror(boolean mirror) { this.mirror = mirror; }
}
