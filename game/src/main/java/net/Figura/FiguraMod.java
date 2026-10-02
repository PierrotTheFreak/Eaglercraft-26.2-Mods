package net.Figura;

import net.Figura.runtime.FiguraRuntime;

/** Entry point for the native Eaglercraft 26.2 Figura port. */
public final class FiguraMod {
    public static final String MOD_ID = "figura";
    public static final String MOD_NAME = "Figura";
    public static final String TARGET_VERSION = "26.2";
    private static boolean initialized;

    private FiguraMod() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        FiguraRuntime.get().start();
        FiguraAssets.validateNamespace();
    }

    public static void tick() { FiguraRuntime.get().tick(); }
    public static void shutdown() { FiguraRuntime.get().shutdown(); initialized = false; }
    public static boolean isInitialized() { return initialized; }
}
