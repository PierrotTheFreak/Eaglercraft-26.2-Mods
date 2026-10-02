package net.Figura.avatar;

/** Metadata kept separate from executable avatar state. */
public record AvatarMetadata(String name, String author, String version, int sizeBytes) {
    public AvatarMetadata {
        name = name == null || name.isBlank() ? "Unnamed Avatar" : name;
        author = author == null ? "Unknown" : author;
        version = version == null ? "unknown" : version;
        if (sizeBytes < 0) throw new IllegalArgumentException("sizeBytes < 0");
    }
}
