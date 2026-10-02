package net.Figura.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads Blockbench Generic Model JSON (.bbmodel) into Figura's model graph.
 *
 * The loader keeps Blockbench's outliner hierarchy, group transforms, cube
 * geometry, per-face UVs, texture references, and export/visibility flags.
 */
public final class BlockbenchModelLoader {
    private BlockbenchModelLoader() {}

    public static void load(byte[] bbmodel, FiguraModel target, String modelName) {
        if (bbmodel == null) throw new IllegalArgumentException("bbmodel == null");
        if (target == null) throw new IllegalArgumentException("target == null");

        JsonObject root = JsonParser.parseString(new String(bbmodel, StandardCharsets.UTF_8)).getAsJsonObject();
        String name = modelName == null || modelName.isBlank() ? "model" : modelName;
        FiguraModelPart modelRoot = target.createPart(unique(target, name), "root");

        Map<String, JsonObject> elements = indexElements(root.getAsJsonArray("elements"));
        Map<Integer, BlockbenchTexture> textures = readTextures(root.getAsJsonArray("textures"));
        JsonArray outliner = root.getAsJsonArray("outliner");
        if (outliner != null) parseOutliner(outliner, target, modelRoot.name(), elements, textures);
    }

    private static Map<String, JsonObject> indexElements(JsonArray array) {
        Map<String, JsonObject> result = new HashMap<>();
        if (array == null) return result;
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject object = element.getAsJsonObject();
            String uuid = string(object, "uuid", null);
            if (uuid != null) result.put(uuid, object);
        }
        return result;
    }

    private static Map<Integer, BlockbenchTexture> readTextures(JsonArray array) {
        Map<Integer, BlockbenchTexture> result = new HashMap<>();
        if (array == null) return result;

        for (int i = 0; i < array.size(); ++i) {
            JsonElement element = array.get(i);
            if (!element.isJsonObject()) continue;
            JsonObject object = element.getAsJsonObject();
            String name = string(object, "name", "texture_" + i);
            String path = string(object, "path", name);
            int width = number(object, "width", 64);
            int height = number(object, "height", 64);
            result.put(i, new BlockbenchTexture(name, path, Math.max(1, width), Math.max(1, height)));
        }
        return result;
    }

    private static void parseOutliner(
            JsonArray array,
            FiguraModel target,
            String parent,
            Map<String, JsonObject> elements,
            Map<Integer, BlockbenchTexture> textures
    ) {
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            parseNode(element.getAsJsonObject(), target, parent, elements, textures);
        }
    }

    private static void parseNode(
            JsonObject node,
            FiguraModel target,
            String parent,
            Map<String, JsonObject> elements,
            Map<Integer, BlockbenchTexture> textures
    ) {
        String name = unique(target, string(node, "name", "part"));
        FiguraModelPart part = target.createPart(name, parent);

        if (node.has("export") && !node.get("export").getAsBoolean()) part.visible(false);
        if (node.has("visibility") && !node.get("visibility").getAsBoolean()) part.visible(false);

        setVector(node.getAsJsonArray("origin"), part.position(), 0.0f);
        setRotation(node.getAsJsonArray("rotation"), part.rotation());
        setVector(node.getAsJsonArray("scale"), part.scale(), 1.0f);

        JsonArray children = node.getAsJsonArray("children");
        if (children == null) return;

        for (JsonElement child : children) {
            if (child.isJsonPrimitive()) {
                String uuid = child.getAsString();
                JsonObject cube = elements.get(uuid);
                if (cube != null) addCube(part, cube, textures);
            } else if (child.isJsonObject()) {
                parseNode(child.getAsJsonObject(), target, name, elements, textures);
            }
        }
    }

    private static void addCube(FiguraModelPart part, JsonObject cube, Map<Integer, BlockbenchTexture> textures) {
        JsonArray from = cube.getAsJsonArray("from");
        JsonArray to = cube.getAsJsonArray("to");
        if (from == null || to == null || from.size() < 3 || to.size() < 3) return;

        float x = number(from, 0, 0);
        float y = number(from, 1, 0);
        float z = number(from, 2, 0);
        float width = number(to, 0, x) - x;
        float height = number(to, 1, y) - y;
        float depth = number(to, 2, z) - z;
        float grow = number(cube, "inflate", 0.0f);
        boolean mirror = cube.has("mirror_uv") && cube.get("mirror_uv").getAsBoolean();

        Map<String, BlockbenchFace> faces = new LinkedHashMap<>();
        JsonObject faceObject = cube.getAsJsonObject("faces");
        String texturePath = null;
        int textureWidth = 64;
        int textureHeight = 64;
        int firstU = 0;
        int firstV = 0;

        if (faceObject != null) {
            for (Map.Entry<String, JsonElement> entry : faceObject.entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                JsonObject face = entry.getValue().getAsJsonObject();
                int textureIndex = number(face, "texture", -1);
                JsonArray uv = face.getAsJsonArray("uv");
                float u0 = number(uv, 0, 0);
                float v0 = number(uv, 1, 0);
                float u1 = number(uv, 2, u0);
                float v1 = number(uv, 3, v0);
                float rotation = number(face, "rotation", 0);
                faces.put(entry.getKey().toLowerCase(Locale.ROOT),
                        new BlockbenchFace(entry.getKey(), textureIndex, u0, v0, u1, v1, rotation));

                if (texturePath == null && textureIndex >= 0) {
                    BlockbenchTexture texture = textures.get(textureIndex);
                    if (texture != null) {
                        texturePath = texture.path();
                        textureWidth = texture.width();
                        textureHeight = texture.height();
                    }
                    firstU = Math.round(u0);
                    firstV = Math.round(v0);
                }
            }
        }

        part.addCube(new FiguraCube(
                x, y, z, width, height, depth,
                firstU, firstV, grow, mirror,
                textureWidth, textureHeight, texturePath, faces
        ));
    }

    private static void setVector(JsonArray array, org.joml.Vector3f out, float fallback) {
        if (array == null || array.size() < 3) {
            out.set(fallback, fallback, fallback);
            return;
        }
        out.set(number(array, 0, fallback), number(array, 1, fallback), number(array, 2, fallback));
    }

    private static void setRotation(JsonArray array, org.joml.Vector3f out) {
        if (array == null || array.size() < 3) {
            out.zero();
            return;
        }
        out.set(number(array, 0, 0), number(array, 1, 0), number(array, 2, 0));
    }

    private static String unique(FiguraModel model, String base) {
        String value = base;
        int suffix = 2;
        while (model.part(value) != null) value = base + "_" + suffix++;
        return value;
    }

    private static String string(JsonObject object, String key, String fallback) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : fallback;
    }

    private static int number(JsonObject object, String key, int fallback) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsInt() : fallback;
    }

    private static float number(JsonObject object, String key, float fallback) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsFloat() : fallback;
    }

    private static float number(JsonArray array, int index, float fallback) {
        if (array == null || index < 0 || index >= array.size()) return fallback;
        JsonElement value = array.get(index);
        return value != null && value.isJsonPrimitive() ? value.getAsFloat() : fallback;
    }
}
