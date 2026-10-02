package net.Figura.avatar;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import net.Figura.model.FiguraModel;
import net.Figura.model.FiguraModelPart;
import net.Figura.permissions.Permission;
import net.Figura.permissions.PermissionSet;

/** Parses the portable portion of a Figura avatar package without Minecraft-specific APIs. */
public final class AvatarLoader {
    public static final int DEFAULT_MAX_BYTES = 100 * 1024;

    private AvatarLoader() {}

    public static Avatar load(UUID owner, AvatarBundle bundle) {
        if (bundle == null) throw new IllegalArgumentException("bundle == null");
        PermissionSet permissions = new PermissionSet();
        permissions.allow(Permission.RENDER);
        permissions.allow(Permission.TICK);
        permissions.allow(Permission.SCRIPT);
        FiguraModel model = new FiguraModel();
        parseParts(bundle.resources().get("model.json"), model);
        return new Avatar(owner, bundle.metadata(), model, permissions);
    }

    public static AvatarBundle readManifest(byte[] manifest, String script, Map<String, byte[]> resources) {
        if (manifest == null) throw new IllegalArgumentException("manifest == null");
        JsonObject json = JsonParser.parseString(new String(manifest, StandardCharsets.UTF_8)).getAsJsonObject();
        String name = string(json, "name", "Unnamed Avatar");
        String author = string(json, "author", "Unknown");
        String version = string(json, "version", "unknown");
        int size = manifest.length + (script == null ? 0 : script.getBytes(StandardCharsets.UTF_8).length);
        return new AvatarBundle(new AvatarMetadata(name, author, version, size), script, resources);
    }

    private static void parseParts(byte[] modelBytes, FiguraModel model) {
        if (modelBytes == null || modelBytes.length == 0) return;
        JsonElement root = JsonParser.parseString(new String(modelBytes, StandardCharsets.UTF_8));
        if (!root.isJsonObject()) return;
        JsonElement parts = root.getAsJsonObject().get("parts");
        if (parts == null || !parts.isJsonArray()) return;
        for (JsonElement element : parts.getAsJsonArray()) {
            if (!element.isJsonObject()) continue;
            JsonObject part = element.getAsJsonObject();
            String name = string(part, "name", null);
            if (name == null || model.part(name) != null) continue;
            String parent = string(part, "parent", "root");
            if (model.part(parent) == null) parent = "root";
            FiguraModelPart created = model.createPart(name, parent);
            created.visible(!part.has("visible") || part.get("visible").getAsBoolean());
        }
    }

    private static String string(JsonObject object, String key, String fallback) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : fallback;
    }
}
