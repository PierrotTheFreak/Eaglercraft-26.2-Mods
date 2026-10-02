package net.Figura.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;

/** Converts the portable Figura graph into the native 26.2 ModelPart tree. */
public final class FiguraModelPartAdapter {
    private FiguraModelPartAdapter() {}

    public static ModelPart bake(FiguraModel model) {
        return bakePart(model.root());
    }

    private static ModelPart bakePart(FiguraModelPart source) {
        java.util.List<ModelPart.Cube> cubes = new java.util.ArrayList<>();
        for (FiguraCube cube : source.cubes()) {
            cubes.add(new ModelPart.Cube(
                cube.u(), cube.v(),
                cube.x(), cube.y(), cube.z(),
                cube.width(), cube.height(), cube.depth(),
                cube.grow(), cube.grow(), cube.grow(),
                cube.mirror(),
                cube.textureWidth(), cube.textureHeight(),
                EnumSet.allOf(Direction.class)
            ));
        }

        Map<String, ModelPart> children = new HashMap<>();
        for (FiguraModelPart child : source.children()) {
            children.put(child.name(), bakePart(child));
        }

        ModelPart part = new ModelPart(cubes, children);
        part.setPos(source.position().x(), source.position().y(), source.position().z());
        part.setRotation(source.rotation().x(), source.rotation().y(), source.rotation().z());
        part.xScale = source.scale().x();
        part.yScale = source.scale().y();
        part.zScale = source.scale().z();
        part.visible = source.visible();
        part.setInitialPose(PartPose.offsetAndRotation(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot));
        return part;
    }
}
