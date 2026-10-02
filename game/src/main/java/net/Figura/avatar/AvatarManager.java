package net.Figura.avatar;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Client-side avatar registry. Network/storage implementations plug into this registry. */
public final class AvatarManager {
    private final Map<UUID, Avatar> avatars = new ConcurrentHashMap<>();

    public Avatar get(UUID owner) { return avatars.get(owner); }
    public Avatar put(Avatar avatar) { return avatars.put(avatar.owner(), avatar); }
    public Avatar remove(UUID owner) { return avatars.remove(owner); }
    public Collection<Avatar> all() { return avatars.values(); }

    public void tick() {
        avatars.values().forEach(Avatar::tick);
    }

    public void clear() {
        avatars.values().forEach(Avatar::unload);
        avatars.clear();
    }
}
