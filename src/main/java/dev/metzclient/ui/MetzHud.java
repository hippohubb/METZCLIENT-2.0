package dev.metzclient.ui;

import dev.metzclient.Metzclient;
import dev.metzclient.module.ClientModule;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class MetzHud {
    private static final int CYAN = 0xFF00D7F5;
    private static final int WHITE = 0xFFF1F5FA;
    private static final int MUTED = 0xFF9BA8B8;

    private MetzHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> render(drawContext));
    }

    private static void render(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;
        int y = 8;
        List<String> lines = new ArrayList<>();

        if (on("Coordinates HUD")) {
            BlockPos p = client.player.getBlockPos();
            lines.add("XYZ  " + p.getX() + "  " + p.getY() + "  " + p.getZ());
        }
        if (on("FPS Display")) lines.add("FPS  " + client.getCurrentFps());
        if (on("Clock HUD")) lines.add("TIME  " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (on("Armor Status")) {
            int armor = client.player.getArmor();
            lines.add("ARMOR  " + armor);
        }
        if (on("Effect Timers")) {
            client.player.getStatusEffects().stream().limit(4).forEach(effect -> {
                String name = effect.getEffectType().value().getName().getString();
                int seconds = effect.getDuration() / 20;
                lines.add(name + "  " + seconds + "s");
            });
        }
        if (on("Waypoint Manager") && !Metzclient.WAYPOINTS.all().isEmpty()) {
            var wp = Metzclient.WAYPOINTS.all().get(Metzclient.WAYPOINTS.all().size() - 1);
            double dx = wp.x() - client.player.getX();
            double dy = wp.y() - client.player.getY();
            double dz = wp.z() - client.player.getZ();
            lines.add("WP " + wp.name() + "  " + Math.round(Math.sqrt(dx*dx + dy*dy + dz*dz)) + "m");
        }
        for (String line : lines) {
            int w = client.textRenderer.getWidth(line);
            ctx.fill(7, y - 2, 17 + w, y + 11, 0xA9101722);
            ctx.fill(7, y - 2, 9, y + 11, CYAN);
            ctx.drawText(client.textRenderer, Text.literal(line), 13, y, WHITE, true);
            y += 15;
        }
    }

    private static boolean on(String name) {
        return Metzclient.MODULES.all().stream().anyMatch(m -> m.name().equals(name) && m.enabled());
    }
}
