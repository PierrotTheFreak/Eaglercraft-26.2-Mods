package net.Figura.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Loads the Generic Model JSON emitted by Blockbench (.bbmodel).
 *
 * <p>The loader accepts both the pre-5.0 outliner representation, where child
 * nodes are embedded objects, and the current representation, where the
 * outliner references UUIDs stored in {@code groups} / {@code elements}.
 * Cubes are converted into Figura's renderer-neutral cube representation.</p>
 */
public final class BlockbenchModelLoader {
    private BlockbenchModelLoader() {}

    public static void load(byte[] bbmodel, FiguraModel target, String modelName) {
        if (bbmodel == null) throw new IllegalArgumentException("bbmodel == null");
        if (target == null) throw new IllegalArgumentException("target == null");

        JsonObject root = JsonParser.parseString(
                new String(bbmodel, StandardCharsets.UTF_8)).getAsJsonObject();

        String name = modelName == null || modelName.isBlank() ? "model" : modelName;
        FiguraModelPart modelRoot = target.createPart(unique(target, name), "root");

        Map<String, JsonObject> nodes = new HashMap<>();
        Map<String, JsonObject> cubes = new HashMap<>();
        indexNodes(root.getAsJsonArray("groups"), nodes);
        indexElements(root.getAsJsonArray("elements"), cubes);

        JsonArray outliner = root.getAsJsonArray("outliner");
        if (outliner != null) {
            parseOutliner(outliner, target, modelRoot.name(), nodes, cubes,
                    new HashSet<>());
        }

        // Older files can contain elements that are not represented as explicit
        // outliner objects. Keep those cubes instead of silently dropping them.
        if (!cubes.isEmpty() && outliner == null) {
            for (JsonObject cube : cubes.values()) {
                addCube(modelRoot, cube, textureSize(root, cube));
            }
        }
    }

    private static void indexNodes(JsonArray array, Map<String, JsonObject> nodes) {
        if (array == null) return;
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject node = element.getAsJsonObject();
            String uuid = string(node, "uuid", null);
            if (uuid != null) nodes.put(uuid, node);
        }
    }

    private static void indexElements(JsonArray array, Map<String, JsonObject> cubes) {
        if (array == null) return;
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject cube = element.getAsJsonObject();
            String uuid = string(cube, "uuid", null);
            if (uuid != null) cubes.put(uuid, cube);
        }
    }

    private static void parseOutliner(
            JsonArray array,
            FiguraModel target,
            String parent,
            Map<String, JsonObject> nodes,
            Map<String, JsonObject> cubes,
            Set<String> visited) {
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                String uuid = element.getAsString();
                if (!visited.add(uuid)) continue;

                JsonObject node = nodes.get(uuid);
                if (node != null) {
                    parseNode(node, target, parent, nodes, cubes, visited);
                } else {
                    JsonObject cube = cubes.get(uuid);
                    if (cube != null) addCube(target.part(parent), cube, textureSize(null, cube));
                }
                continue;
            }

            if (!element.isJsonObject()) continue;
            parseNode(element.getAsJsonObject(), target, parent, nodes, cubes, visited);
        }
    }

    private static void parseNode(
            JsonObject node,
            FiguraModel target,
            String parent,
            Map<String, JsonObject> nodes,
            Map<String, JsonObject> cubes,
            Set<String> visited) {
        String uuid = string(node, "uuid", null);
        if (uuid != null && !visited.add(uuid)) return;

        // Some older bbmodels store cube data directly in the outliner.
        boolean cube = node.has("from") && node.has("to");
        if (cube) {
            FiguraModelPart owner = target.part(parent);
            if (owner != null) addCube(owner, node, textureSize(null, node));
            return;
        }

        String name = string(node, "name", "part");
        String partName = unique(target, name);
        FiguraModelPart part = target.createPart(partName, parent);

        applyTransform(part, node);
        if (node.has("export") && !node.get("export").getAsBoolean()) {
            part.visible(false);
        }

        JsonArray children = node.getAsJsonArray("children");
        if (children == null) return;

        for (JsonElement child : children) {
            if (child.isJsonPrimitive()) {
                String childUuid = child.getAsString();
                if (!visited.add(childUuid)) continue;

                JsonObject childNode = nodes.get(childUuid);
                if (childNode != null) {
                    parseNode(childNode, target, partName, nodes, cubes, visited);
                    continue;
                }

                JsonObject childCube = cubes.get(childUuid);
                if (childCube != null) {
                    addCube(target.part(partName), childCube, textureSize(null, childCube));
                }
            } else if (child.isJsonObject()) {
                parseNode(child.getAsJsonObject(), target, partName, nodes, cubes, visited);
            }
        }
    }

    private static void addCube(FiguraModelPart part, JsonObject cube, int[] textureSize) {
        if (part == null || cube == null) return;

        JsonArray from = cube.getAsJsonArray("from");
        JsonArray to = cube.getAsJsonArray("to");
        if (from == null || to == null || from.size() < 3 || to.size() < 3) return;

        float x = number(from, 0);
        float y = number(from, 1);
        float z = number(from, 2);
        float width = number(to, 0) - x;
        float height = number(to, 1) - y;
        float depth = number(to, 2) - z;

        int u = 0;
        int v = 0;
        JsonArray uv = cube.getAsJsonArray("uv_offset");
        if (uv != null && uv.size() >= 2) {
            u = Math.round(number(uv, 0));
            v = Math.round(number(uv, 1));
        } else {
            JsonObject faces = cube.getAsJsonObject("faces");
            JsonObject north = faces == null ? null : faces.getAsJsonObject("north");
            JsonArray faceUv = north == null ? null : north.getAsJsonArray("uv");
            if (faceUv != null && faceUv.size() >= 2) {
                u = Math.round(number(faceUv, 0));
                v = Math.round(number(faceUv, 1));
            }
        }

        float grow = number(cube, "inflate", 0.0F);
        boolean mirror = cube.has("mirror_uv") && cube.get("mirror_uv").getAsBoolean();

        part.addCube(new FiguraCube(
                x, y, z, width, height, depth,
                u, v, grow, mirror,
                textureSize[0], textureSize[1]
        ));
    }

    private static void applyTransform(FiguraModelPart part, JsonObject node) {
        JsonArray origin = node.getAsJsonArray("origin");
        if (origin != null && origin.size() >= 3) {
            part.position().set(number(origin, 0), number(origin, 1), number(origin, 2));
        }

        JsonArray rotation = node.getAsJsonArray("rotation");
        if (rotation != null && rotation.size() >= 3) {
            // Blockbench stores degrees; ModelPart/JOML uses radians.
            part.rotation().set(
                    (float) Math.toRadians(number(rotation, 0)),
                    (float) Math.toRadians(number(rotation, 1)),
                    (float) Math.toRadians(number(rotation, 2))
            );
        }

        JsonArray scale = node.getAsJsonArray("scale");
        if (scale != null && scale.size() >= 3) {
            part.scale().set(number(scale, 0), number(scale, 1), number(scale, 2));
        }
    }

    private static int[] textureSize(JsonObject root, JsonObject cube) {
        // Generic Model 4.9+ stores texture UV dimensions per texture.
        // Fall back to the project resolution used by older bbmodels.
        if (root != null) {
            JsonArray textures = root.getAsJsonArray("textures");
            if (textures != null) {
                for (JsonElement element : textures) {
                    if (!element.isJsonObject()) continue;
                    JsonObject texture = element.getAsJsonObject();
                    int width = integer(texture, "uv_width", 0);
                    int height = integer(texture, "uv_height", 0);
                    if (width > 0 && height > 0) return new int[] {width, height};
                }
            }

            JsonObject resolution = root.getAsJsonObject("resolution");
            if (resolution != null) {
                int width = integer(resolution, "width", 64);
                int height = integer(resolution, "height", 64);
                return new int[] {Math.max(1, width), Math.max(1, height)};
            }
        }

        return new int[] {64, 64};
    }

    private static String unique(FiguraModel model, String base) {
        String clean = base == null || base.isBlank() ? "part" : base;
        String value = clean;
        int suffix = 2;
        while (model.part(value) != null) value = clean + "_" + suffix++;
        return value;
    }

    private static String string(JsonObject object, String key, String fallback) {
        if (object == null || !object.has(key)) return fallback;
        JsonElement value = object.get(key);
        return value.isJsonPrimitive() ? value.getAsString() : fallback;
    }

    private static int integer(JsonObject object, String key, int fallback) {
        if (object == null || !object.has(key)) return fallback;
        try {
            return object.get(key).getAsInt();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static float number(JsonObject object, String key, float fallback) {
        if (object == null || !object.has(key)) return fallback;
        try {
            return object.get(key).getAsFloat();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static float number(JsonArray array, int index) {
        try {
            return array.get(index).getAsFloat();
        } catch (RuntimeException ignored) {
            return 0.0F;
        }
    }
}
