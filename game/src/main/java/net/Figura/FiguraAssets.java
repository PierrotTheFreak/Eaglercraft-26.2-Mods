package net.Figura;

/** Central resource namespace for the Figura 26.2 port. */
public final class FiguraAssets {
    public static final String NAMESPACE = "figura";
    public static final String ROOT = "assets/figura/";
    public static final String TEXTURES = ROOT + "textures/";
    public static final String GUI = TEXTURES + "gui/";
    public static final String MODELS = TEXTURES + "models/";
    public static final String LANG = ROOT + "lang/";
    public static final String SCRIPTS = ROOT + "scripts/";
    private FiguraAssets() {}
    public static void validateNamespace() {
        if (!NAMESPACE.equals("figura")) throw new IllegalStateException("Invalid Figura resource namespace");
    }
}
