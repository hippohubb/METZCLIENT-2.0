package dev.metzclient.render;

import dev.metzclient.Metzclient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/** Client-side outlines for nearby container blocks in already-loaded chunks. */
public final class WorldOverlay {
    private static final Set<Block> CONTAINERS = Set.of(
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.ENDER_CHEST,
        Blocks.SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX,
        Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX,
        Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
        Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
        Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.HOPPER, Blocks.DISPENSER,
        Blocks.DROPPER, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER
    );

    private WorldOverlay() {}

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null || !enabled("Storage ESP")) return;
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();
            BlockPos center = client.player.getBlockPos();
            int radius = 12;
            var consumers = context.consumers();
            if (consumers == null) return;
            VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);
            BlockPos.stream(center.add(-radius, -radius, -radius), center.add(radius, radius, radius))
                .filter(pos -> CONTAINERS.contains(client.world.getBlockState(pos).getBlock()))
                .limit(180)
                .forEach(pos -> {
                    Box box = new Box(pos).expand(0.002);
                    net.minecraft.client.render.WorldRenderer.drawBox(
                        matrices, lines, box, 0.0F, 0.84F, 1.0F, 0.95F
                    );
                });
            matrices.pop();
        });
    }

    private static boolean enabled(String name) {
        return Metzclient.MODULES.all().stream().anyMatch(m -> m.name().equals(name) && m.enabled());
    }
}
