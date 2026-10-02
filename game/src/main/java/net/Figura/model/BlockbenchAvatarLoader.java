package net.Figura.model;

import java.util.Map;

/** Loads every .bbmodel in an avatar package into the shared model namespace. */
public final class BlockbenchAvatarLoader {
    private BlockbenchAvatarLoader() {}

    public static FiguraModel load(Map<String, byte[]> files) {
        FiguraModel model = new FiguraModel();
        if (files == null) return model;
        files.entrySet().stream()
            .filter(e -> e.getKey().toLowerCase().endsWith(".bbmodel"))
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> {
                String path = e.getKey().replace('\\', '/');
                String name = path.substring(path.lastIndexOf('/') + 1, path.length() - ".bbmodel".length());
                BlockbenchModelLoader.load(e.getValue(), model, name);
            });
        return model;
    }
}
