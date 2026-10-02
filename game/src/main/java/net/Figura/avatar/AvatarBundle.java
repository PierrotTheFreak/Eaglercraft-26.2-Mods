package net.Figura.avatar;

import java.util.Collections;
import java.util.Map;

/** In-memory avatar package after archive/resource extraction. */
public record AvatarBundle(AvatarMetadata metadata, String script, Map<String, byte[]> resources) {
    public AvatarBundle {
        resources = resources == null ? Map.of() : Map.copyOf(resources);
        script = script == null ? "" : script;
    }
    public Map<String, byte[]> resources() { return Collections.unmodifiableMap(resources); }
}
