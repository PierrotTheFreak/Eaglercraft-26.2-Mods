package net.Figura.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Loads the Generic Model JSON emitted by Blockbench (.bbmodel).
 * Figura treats each bbmodel as a model-part subtree, so this loader preserves
 * the outliner hierarchy instead of flattening cubes into one mesh.
 */
public final class BlockbenchModelLoader {
    private BlockbenchModelLoader() {}

    public static void load(byte[] bbmodel, FiguraModel target, String modelName) {
        if (bbmodel == null) throw new IllegalArgumentException("bbmodel == null");
        JsonObject root = JsonParser.parseString(new String(bbmodel, StandardCharsets.UTF_8)).getAsJsonObject();
        String name = modelName == null || modelName.isBlank() ? "model" : modelName;
        FiguraModelPart modelRoot = target.createPart(unique(target, name), "root");
        JsonArray outliner = root.getAsJsonArray("outliner");
        if (outliner != null) parseOutliner(outliner, target, modelRoot.name());
    }

    private static void parseOutliner(JsonArray array, FiguraModel target, String parent) {
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject node = element.getAsJsonObject();
            String name = node.has("name") ? node.get("name").getAsString() : "part";
            name = unique(target, name);
            FiguraModelPart part = target.createPart(name, parent);
            if (node.has("export") && !node.get("export").getAsBoolean()) part.visible(false);
            JsonArray children = node.getAsJsonArray("children");
            if (children != null) parseOutliner(children, target, name);
        }
    }

    private static String unique(FiguraModel model, String base) {
        String value = base;
        int suffix = 2;
        while (model.part(value) != null) value = base + "_" + suffix++;
        return value;
    }
}
