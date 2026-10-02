package net.Figura.runtime;

import net.Figura.animation.AnimationPlayer;
import net.Figura.avatar.AvatarManager;

/** Shared Figura runtime; Minecraft/Eagler integration remains at the edge. */
public final class FiguraRuntime {
    private static final FiguraRuntime INSTANCE = new FiguraRuntime();
    private final AvatarManager avatars = new AvatarManager();
    private final AnimationPlayer animations = new AnimationPlayer();
    private boolean started;
    private FiguraRuntime() {}
    public static FiguraRuntime get() { return INSTANCE; }
    public AvatarManager avatars() { return avatars; }
    public AnimationPlayer animations() { return animations; }
    public boolean started() { return started; }
    public void start() { started = true; }
    public void tick() { if (started) { avatars.tick(); animations.tick(1f); } }
    public void shutdown() { avatars.clear(); started = false; }
}
