package net.Figura.avatar;

import java.util.UUID;
import net.Figura.model.FiguraModel;
import net.Figura.permissions.PermissionSet;

/** Runtime representation of one Figura avatar. */
public final class Avatar {
    private final UUID owner;
    private final FiguraModel model;
    private final PermissionSet permissions;
    private final AvatarMetadata metadata;
    private AvatarState state = AvatarState.UNLOADED;
    private int tick;

    public Avatar(UUID owner, AvatarMetadata metadata, FiguraModel model, PermissionSet permissions) {
        this.owner = owner;
        this.metadata = metadata;
        this.model = model;
        this.permissions = permissions;
    }

    public UUID owner() { return owner; }
    public FiguraModel model() { return model; }
    public PermissionSet permissions() { return permissions; }
    public AvatarMetadata metadata() { return metadata; }
    public AvatarState state() { return state; }
    public int tickCount() { return tick; }

    public void load() { state = AvatarState.LOADED; }
    public void unload() { state = AvatarState.UNLOADED; }
    public void fail() { state = AvatarState.ERROR; }

    public void tick() {
        if (state != AvatarState.LOADED) return;
        tick++;
        model.tick(tick);
    }
}
