package net.Figura.model;

/** Texture entry referenced by a Blockbench model. */
public record BlockbenchTexture(String name, String path, int width, int height) {
    public BlockbenchTexture {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("blank texture name");
        if (width < 1 || height < 1) throw new IllegalArgumentException("invalid texture dimensions");
    }
}
