package net.Figura.render;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.Figura.avatar.Avatar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/** Registers avatar-owned PNGs as client textures and reuses them across frames. */
public final class FiguraTextureRegistry {
    private static final Map<UUID, Identifier> TEXTURES = new ConcurrentHashMap<>();

    private FiguraTextureRegistry() {}

    public static Identifier getOrCreate(Avatar avatar) {
        Identifier existing = TEXTURES.get(avatar.owner());
        if (existing != null) return existing;

        byte[] png = findPng(avatar.resources());
        if (png == null) return null;

        try {
            NativeImage image = NativeImage.read(png);
            Identifier id = Identifier.fromNamespaceAndPath(
                    "figura", "avatars/" + avatar.owner().toString().replace('-', '_'));
            Minecraft.getInstance().getTextureManager().register(
                    id, new DynamicTexture(() -> "Figura avatar " + avatar.owner(), image));
            Identifier previous = TEXTURES.putIfAbsent(avatar.owner(), id);
            if (previous != null) {
                Minecraft.getInstance().getTextureManager().release(id);
                return previous;
            }
            return id;
        } catch (IOException ignored) {
            return null;
        }
    }

    public static void release(UUID owner) {
        Identifier id = TEXTURES.remove(owner);
        if (id != null) Minecraft.getInstance().getTextureManager().release(id);
    }

    private static byte[] findPng(Map<String, byte[]> resources) {
        for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
            String path = entry.getKey().toLowerCase(java.util.Locale.ROOT);
            if (path.endsWith(".png") && entry.getValue() != null) return entry.getValue();
        }
        return null;
    }
}
