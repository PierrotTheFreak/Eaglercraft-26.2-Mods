package net.Figura.model;

/** A Figura cuboid in model-local 16-unit Minecraft coordinates. */
public record FiguraCube(
        float x, float y, float z,
        float width, float height, float depth,
        int u, int v,
        float grow,
        boolean mirror,
        int textureWidth,
        int textureHeight
) {
    public FiguraCube {
        if (width < 0 || height < 0 || depth < 0) throw new IllegalArgumentException("negative cube size");
        if (textureWidth < 1 || textureHeight < 1) throw new IllegalArgumentException("invalid texture dimensions");
    }

    public FiguraCube(float x, float y, float z, float width, float height, float depth,
                      int u, int v, float grow, boolean mirror) {
        this(x, y, z, width, height, depth, u, v, grow, mirror, 64, 64);
    }
}
