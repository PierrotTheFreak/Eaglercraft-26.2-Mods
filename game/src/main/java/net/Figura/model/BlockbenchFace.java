package net.Figura.model;

/** One Blockbench cube face's texture mapping. */
public record BlockbenchFace(
        String direction,
        int textureIndex,
        float u0,
        float v0,
        float u1,
        float v1,
        float rotation
) {
    public boolean textured() {
        return textureIndex >= 0;
    }
}
