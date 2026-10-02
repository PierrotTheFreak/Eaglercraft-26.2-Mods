package net.Figura.render;

import net.Figura.avatar.Avatar;
import net.Figura.avatar.AvatarState;
import net.Figura.permissions.Permission;

/** Renderer-neutral contract for the Eaglercraft 26.2 renderer adapter. */
public interface FiguraRenderBridge {
    boolean render(Avatar avatar, Object renderContext);
    default boolean canRender(Avatar avatar) {
        return avatar != null && avatar.state() == AvatarState.LOADED
            && avatar.permissions().allows(Permission.RENDER);
    }
}
