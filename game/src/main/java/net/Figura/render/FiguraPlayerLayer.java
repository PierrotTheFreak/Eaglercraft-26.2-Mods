package net.Figura.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.Figura.avatar.Avatar;
import net.Figura.avatar.AvatarManager;
import net.Figura.avatar.AvatarState;
import net.Figura.model.FiguraModelPartAdapter;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Submits Figura geometry through the native 26.2 feature/render pipeline. */
public final class FiguraPlayerLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final AvatarManager avatars;

    public FiguraPlayerLayer(AvatarRenderer<?> parent, AvatarManager avatars) {
        super(parent);
        this.avatars = avatars;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       AvatarRenderState state, float limbAngle, float limbDistance) {
        Avatar avatar = avatars.get(state.figuraOwner);
        if (avatar == null || avatar.state() != AvatarState.LOADED) return;
        var part = FiguraModelPartAdapter.bake(avatar.model());
        collector.submitModelPart(part, poseStack, RenderTypes.entityTranslucent(state.skin.body().texturePath()),
            lightCoords, OverlayTexture.NO_OVERLAY, null);
    }
}
