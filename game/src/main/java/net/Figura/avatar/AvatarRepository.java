package net.Figura.avatar;

import java.util.UUID;
import java.util.function.Consumer;

/** Storage/network boundary for local and remote avatars. */
public interface AvatarRepository {
    void load(UUID owner, Consumer<Avatar> success, Consumer<Throwable> failure);
    void save(UUID owner, Avatar avatar, Consumer<Void> success, Consumer<Throwable> failure);
}
