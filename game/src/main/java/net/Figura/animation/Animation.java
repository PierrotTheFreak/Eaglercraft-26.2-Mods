package net.Figura.animation;

import java.util.Objects;

/** Lightweight animation definition used by the 26.2 runtime. */
public final class Animation {
    private final String name;
    private final int lengthTicks;
    private final boolean looping;
    private float time;
    private boolean playing;

    public Animation(String name, int lengthTicks, boolean looping) {
        this.name = Objects.requireNonNull(name);
        if (lengthTicks < 1) throw new IllegalArgumentException("lengthTicks < 1");
        this.lengthTicks = lengthTicks;
        this.looping = looping;
    }

    public String name() { return name; }
    public int lengthTicks() { return lengthTicks; }
    public boolean looping() { return looping; }
    public float time() { return time; }
    public boolean playing() { return playing; }
    public void play() { playing = true; }
    public void stop() { playing = false; time = 0f; }

    public void tick(float delta) {
        if (!playing) return;
        time += delta;
        if (time >= lengthTicks) {
            if (looping) time %= lengthTicks;
            else { time = lengthTicks; playing = false; }
        }
    }
}
