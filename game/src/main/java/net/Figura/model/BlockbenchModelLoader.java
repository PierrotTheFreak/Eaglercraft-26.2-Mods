package net.Figura.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loads the Generic Model JSON emitted by Blockbench (.bbmodel).
 *
 * Blockbench's outliner can reference cubes by UUID instead of embedding them,
 * so the loader indexes the top-level elements first and then walks the
 * outliner while preserving the model-part hierarchy.
 */
public final class BlockbenchModelLoader {
    private BlockbenchModelLoader() {}

    public static void load(byte[] bbmodel, FiguraModel target, String modelName) {
        if (bbmodel == null) throw new IllegalArgumentException("bbmodel == null");
        JsonObject root = JsonParser.parseString(new String(bbmodel, StandardCharsets.UTF_8)).getAsJsonObject();
        String name = modelName == null || modelName.isBlank() ? "model" : modelName;

        TextureInfo texture = readTextureInfo(root);
        Map<String, JsonObject> elements = indexElements(root);
        FiguraModelPart modelRoot = target.createPart(unique(target, name), "root");

        JsonArray outliner = root.getAsJsonArray("outliner");
        if (outliner != null) {
            parseOutliner(outliner, target, modelRoot.name(), elements, texture);
        }
    }

    private static Map<String, JsonObject> indexElements(JsonObject root) {
        Map<String, JsonObject> result = new HashMap<>();
        JsonArray elements = root.getAsJsonArray("elements");
        if (elements == null) return result;

        for (JsonElement element : elements) {
            if (!element.isJsonObject()) continue;
            JsonObject object = element.getAsJsonObject();
            if (object.has("uuid")) result.put(object.get("uuid").getAsString(), object);
            else if (object.has("name")) result.put(object.get("name").getAsString(), object);
        }
        return result;
    }

    private static void parseOutliner(
            JsonArray array,
            FiguraModel target,
            String parent,
            Map<String, JsonObject> elements,
            TextureInfo texture
    ) {
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                JsonObject cube = elements.get(element.getAsString());
                if (cube != null) addCubeNode(target, parent, cube, texture);
                continue;
            }
            if (!element.isJsonObject()) continue;

            JsonObject node = element.getAsJsonObject();

            // Some Blockbench exports embed a cube object directly in the outliner.
            if (node.has("from") && node.has("to")) {
                addCubeNode(target, parent, node, texture);
                continue;
            }

            String name = node.has("name") ? node.get("name").getAsString() : "part";
            name = unique(target, name);
            FiguraModelPart part = target.createPart(name, parent);

            if (node.has("export") && !node.get("export").getAsBoolean()) {
                part.visible(false);
            }
            readTransform(node, part);

            JsonArray children = node.getAsJsonArray("children");
            if (children != null) {
                parseOutliner(children, target, name, elements, texture);
            }
        }
    }

    private static void addCubeNode(
            FiguraModel target,
            String parent,
            JsonObject cube,
            TextureInfo texture
    ) {
        String baseName = cube.has("name") ? cube.get("name").getAsString() : "cube";
        String name = unique(target, baseName);
        FiguraModelPart part = target.createPart(name, parent);

        if (cube.has("export") && !cube.get("export").getAsBoolean()) {
            part.visible(false);
        }
        readTransform(cube, part);

        FiguraCube parsed = parseCube(cube, texture);
        if (parsed != null) part.addCube(parsed);
    }

    private static void readTransform(JsonObject node, FiguraModelPart part) {
        float[] origin = vector(node, "origin", 0f, 0f, 0f);
        float[] rotation = vector(node, "rotation", 0f, 0f, 0f);

        // Blockbench stores Euler angles in degrees; Minecraft ModelPart uses radians.
        part.position().set(origin[0], origin[1], origin[2]);
        part.rotation().set(
            (float) Math.toRadians(rotation[0]),
            (float) Math.toRadians(rotation[1]),
            (float) Math.toRadians(rotation[2])
        );

        if (node.has("scale")) {
            float[] scale = vector(node, "scale", 1f, 1f, 1f);
            part.scale().set(scale[0], scale[1], scale[2]);
        }
    }

    private static FiguraCube parseCube(JsonObject cube, TextureInfo texture) {
        JsonArray from = cube.getAsJsonArray("from");
        JsonArray to = cube.getAsJsonArray("to");
        if (from == null || to == null || from.size() < 3 || to.size() < 3) return null;

        float x1 = from.get(0).getAsFloat();
        float y1 = from.get(1).getAsFloat();
        float z1 = from.get(2).getAsFloat();
        float x2 = to.get(0).getAsFloat();
        float y2 = to.get(1).getAsFloat();
        float z2 = to.get(2).getAsFloat();

        float x = Math.min(x1, x2);
        float y = Math.min(y1, y2);
        float z = Math.min(z1, z2);
        float width = Math.abs(x2 - x1);
        float height = Math.abs(y2 - y1);
        float depth = Math.abs(z2 - z1);

        int u = 0;
        int v = 0;
        boolean foundUv = false;

        JsonObject faces = cube.getAsJsonObject("faces");
        if (faces != null) {
            // Prefer north because it is stable and matches Blockbench's default
            // box-UV front reference. Other faces are still retained by the same
            // atlas coordinates through ModelPart's cube renderer.
            String[] preferred = {"north", "south", "east", "west", "up", "down"};
            for (String side : preferred) {
                JsonObject face = faces.getAsJsonObject(side);
                if (face == null) continue;
                JsonArray uv = face.getAsJsonArray("uv");
                if (uv != null && uv.size() >= 4) {
                    u = Math.round(uv.get(0).getAsFloat());
                    v = Math.round(uv.get(1).getAsFloat());
                    foundUv = true;
                    break;
                }
            }
        }

        if (!foundUv && cube.has("uv_offset")) {
            JsonArray uv = cube.getAsJsonArray("uv_offset");
            if (uv != null && uv.size() >= 2) {
                u = Math.round(uv.get(0).getAsFloat());
                v = Math.round(uv.get(1).getAsFloat());
            }
        }

        boolean mirror = cube.has("mirror") && cube.get("mirror").getAsBoolean();
        float grow = cube.has("inflate") ? cube.get("inflate").getAsFloat() : 0f;
        return new FiguraCube(x, y, z, width, height, depth, u, v, grow, mirror,
            texture.width, texture.height);
    }

    private static TextureInfo readTextureInfo(JsonObject root) {
        JsonArray textures = root.getAsJsonArray("textures");
        if (textures == null || textures.isEmpty()) return new TextureInfo(64, 64);

        int width = 64;
        int height = 64;
        for (JsonElement element : textures) {
            if (!element.isJsonObject()) continue;
            JsonObject texture = element.getAsJsonObject();
            if (texture.has("uv_width")) width = Math.max(1, texture.get("uv_width").getAsInt());
            if (texture.has("uv_height")) height = Math.max(1, texture.get("uv_height").getAsInt());
            if (texture.has("width")) width = Math.max(1, texture.get("width").getAsInt());
            if (texture.has("height")) height = Math.max(1, texture.get("height").getAsInt());
            break;
        }
        return new TextureInfo(width, height);
    }

    private static float[] vector(JsonObject object, String key, float x, float y, float z) {
        JsonArray array = object.getAsJsonArray(key);
        if (array == null || array.size() < 3) return new float[] {x, y, z};
        return new float[] {array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    private static String unique(FiguraModel model, String base) {
        String safeBase = base == null || base.isBlank() ? "part" : base;
        String value = safeBase;
        int suffix = 2;
        while (model.part(value) != null) value = safeBase + "_" + suffix++;
        return value;
    }

    private record TextureInfo(int width, int height) {}
}
