package net.Figura.animation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimationPlayer {
    private final Map<String, Animation> animations = new ConcurrentHashMap<>();

    public void register(Animation animation) { animations.put(animation.name(), animation); }
    public Animation get(String name) { return animations.get(name); }
    public void play(String name) { Animation a = get(name); if (a != null) a.play(); }
    public void stop(String name) { Animation a = get(name); if (a != null) a.stop(); }
    public void tick(float delta) { animations.values().forEach(a -> a.tick(delta)); }
}
