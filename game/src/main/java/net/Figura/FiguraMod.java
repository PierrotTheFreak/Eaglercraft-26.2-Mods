package net.Figura;

/**
 * Figura for Eaglercraft 26.2.
 *
 * <p>This is the 26.2-native entry point. The port intentionally does not
 * depend on Fabric/Forge/Architectury APIs from upstream Figura; those APIs
 * are replaced incrementally with Eaglercraft-native hooks.</p>
 */
public final class FiguraMod {
    public static final String MOD_ID = "figura";
    public static final String MOD_NAME = "Figura";
    public static final String TARGET_VERSION = "26.2";
    private static boolean initialized;
    private FiguraMod() {}
    public static void init() {
        if (initialized) return;
        initialized = true;
        FiguraAssets.validateNamespace();
    }
    public static boolean isInitialized() { return initialized; }
}
