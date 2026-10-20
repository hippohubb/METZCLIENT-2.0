package dev.metzclient.ui;

import dev.metzclient.Metzclient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Procedural CS-inspired first-person HUD knife with animated inspect sequences. */
public final class KnifeCosmeticOverlay {
    private static long inspectStarted = -1L;
    private static long lastInspect = 0L;
    private static long lastAttack = 0L;
    private static boolean butterflyOpen = true;

    private KnifeCosmeticOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    public static void inspect() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (!enabled("Butterfly Knife Cosmetic") && !enabled("Bayonet Cosmetic")) {
            client.player.sendMessage(Text.literal("§bMetzclient §7Enable Butterfly Knife Cosmetic or Bayonet Cosmetic in the menu first."), true);
            return;
        }
        inspectStarted = System.currentTimeMillis();
        lastInspect = inspectStarted;
        butterflyOpen = !butterflyOpen;
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        boolean butterfly = enabled("Butterfly Knife Cosmetic");
        boolean bayonet = !butterfly && enabled("Bayonet Cosmetic");
        if (!butterfly && !bayonet) return;

        long now = System.currentTimeMillis();
        // Vanilla attack input triggers a short slash flourish. It does not alter damage or combat.
        if (mc.options.attackKey.isPressed() && now - lastAttack > 360) lastAttack = now;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        float idle = (float)(now % 1800L) / 1800f;
        float bob = (float)Math.sin(idle * Math.PI * 2) * 2.2f;
        float inspectT = inspectStarted < 0 ? -1f : (now - inspectStarted) / 1120f;
        if (inspectT > 1f) { inspectStarted = -1L; inspectT = -1f; }
        float slashT = (now - lastAttack) / 360f;
        boolean slashing = slashT >= 0f && slashT < 1f;
        float x = sw - 116f;
        float y = sh - 112f + bob;
        float rotation = 0f;
        float lift = 0f;
        float scale = 1f;
        if (inspectT >= 0f) {
            // Three-part inspect: lift, flip/spin, then settle back into the idle pose.
            float t = easeInOut(inspectT);
            lift = (float)Math.sin(Math.min(1f, inspectT) * Math.PI) * 25f;
            rotation = (float)Math.sin(inspectT * Math.PI * 2.0) * 0.9f;
            if (inspectT < .28f) rotation += inspectT / .28f * 1.1f;
            else if (inspectT < .68f) rotation += (float)Math.sin((inspectT - .28f) / .40f * Math.PI * 2) * 1.5f;
            else rotation *= 1f - (inspectT - .68f) / .32f;
            scale += (float)Math.sin(t * Math.PI) * .12f;
        }
        if (slashing) {
            float punch = (float)Math.sin(slashT * Math.PI);
            x -= punch * 44f;
            y += punch * 22f;
            rotation -= punch * 1.15f;
        }
        int px = Math.round(x), py = Math.round(y - lift);
        ctx.fill(px - 16, py - 12, px + 92, py + 82, 0x20000000);
        if (bayonet) drawBayonet(ctx, px, py, rotation, scale, inspectT, slashing);
        else drawButterfly(ctx, px, py, rotation, scale, inspectT, slashing);
        String label = butterfly ? "METZ | BUTTERFLY" : "METZ | BAYONET";
        ctx.drawText(mc.textRenderer, label, px - 7, py + 82, 0xDFFFFFFF, false);
        ctx.drawText(mc.textRenderer, inspectT >= 0 ? "INSPECTING..." : "G  INSPECT", px - 7, py + 93, 0xFF00D7F5, false);
    }

    private static void drawButterfly(DrawContext c, int x, int y, float rotation, float scale, float inspectT, boolean slashing) {
        int steel = 0xFFE1ECF4, edge = 0xFF8DAABD, dark = 0xFF1A2531, gripA = 0xFF101722, gripB = 0xFF26384A;
        float openT = inspectT < 0 ? (butterflyOpen ? 1f : 0f) : smoothstep(Math.min(1f, inspectT / .32f));
        float closeT = inspectT > .68f ? smoothstep((inspectT - .68f) / .32f) : 0f;
        float open = inspectT < 0 ? openT : openT * (1f - closeT) + (butterflyOpen ? 1f : 0f) * closeT;
        float spin = rotation + (1f - open) * .45f;
        // Blade rotates around the pivot while the two handles fan out during the flip.
        lineRot(c, x + 39, y + 39, -9, -37, 7, steel, spin, scale);
        lineRot(c, x + 39, y + 39, -8, -37, 2, edge, spin, scale);
        lineRot(c, x + 39, y + 39, -3, -28, 2, 0xFFB7CADA, spin, scale);
        lineRot(c, x + 39, y + 39, -11, -5, 14, dark, spin, scale);
        float a = (float)(Math.PI * (.08 + .42 * open));
        float b = (float)(Math.PI * (.92 - .42 * open));
        handle(c, x + 39, y + 39, 27, a + rotation, gripA, edge, scale);
        handle(c, x + 39, y + 39, 27, b + rotation, gripB, 0xFF607C91, scale);
        // Pivot screws and a cyan glint pulse on inspect.
        dot(c, x + 39, y + 39, 5, 0xFF00D7F5, scale);
        dot(c, x + 39, y + 39, 2, 0xFFE9FCFF, scale);
        if (inspectT >= 0f) {
            int glow = (int)(100 + 100 * Math.sin(inspectT * Math.PI * 8));
            c.fill(x + 23, y + 32, x + 55, y + 34, (Math.max(0, Math.min(255, glow)) << 24) | 0x00D7F5);
        }
        if (slashing) lineRot(c, x + 39, y + 39, -5, -43, 2, 0xAA9AF7FF, spin - .15f, scale);
    }

    private static void drawBayonet(DrawContext c, int x, int y, float rotation, float scale, float inspectT, boolean slashing) {
        int steel = 0xFFE4EDF5, edge = 0xFF8BA9BF, dark = 0xFF26313E, handle = 0xFF121923, grip = 0xFF1B2837;
        // Rendered as a rotated collection of tapered strokes so the inspect animation visibly turns the knife.
        lineRot(c, x + 42, y + 51, -7, -46, 13, steel, rotation, scale);
        lineRot(c, x + 42, y + 51, -7, -46, 3, edge, rotation, scale);
        lineRot(c, x + 42, y + 51, -2, -38, 2, 0xFFB9CADA, rotation, scale);
        lineRot(c, x + 42, y + 51, -12, -5, 24, dark, rotation, scale);
        lineRot(c, x + 42, y + 51, -7, 0, 14, handle, rotation, scale);
        lineRot(c, x + 42, y + 51, 0, 1, 14, grip, rotation, scale);
        for (int i = 0; i < 5; i++) lineRot(c, x + 42, y + 51, -6, 5 + i * 5, 12, i % 2 == 0 ? grip : 0xFF33485B, rotation, scale);
        lineRot(c, x + 42, y + 51, -7, 28, 14, dark, rotation, scale);
        dot(c, x + 42, y + 51, 4, inspectT >= 0 ? 0xFF00D7F5 : edge, scale);
        if (slashing) lineRot(c, x + 42, y + 51, -4, -53, 2, 0xAA9AF7FF, rotation, scale);
    }

    private static void handle(DrawContext c, float px, float py, float length, float angle, int color, int accent, float scale) {
        float ex = px + (float)Math.cos(angle) * length;
        float ey = py + (float)Math.sin(angle) * length;
        segment(c, px, py, ex, ey, 9 * scale, color);
        segment(c, px, py, ex, ey, 2 * scale, accent);
        for (int i = 1; i <= 4; i++) {
            float t = i / 5f;
            float gx = px + (ex - px) * t;
            float gy = py + (ey - py) * t;
            dot(c, gx, gy, 2.1f * scale, 0xFF0A111A, 1f);
        }
    }

    private static void lineRot(DrawContext c, float px, float py, float dx, float dy, float width, int color, float angle, float scale) {
        float rx = (float)(dx * Math.cos(angle) - dy * Math.sin(angle));
        float ry = (float)(dx * Math.sin(angle) + dy * Math.cos(angle));
        segment(c, px, py, px + rx * scale, py + ry * scale, width * scale, color);
    }

    private static void segment(DrawContext c, float x1, float y1, float x2, float y2, float width, int color) {
        int steps = Math.max(2, (int)(Math.hypot(x2 - x1, y2 - y1) * 1.5));
        int half = Math.max(1, Math.round(width / 2f));
        for (int i = 0; i <= steps; i++) {
            float t = i / (float)steps;
            int x = Math.round(x1 + (x2 - x1) * t), y = Math.round(y1 + (y2 - y1) * t);
            c.fill(x - half, y - half, x + half + 1, y + half + 1, color);
        }
    }

    private static void dot(DrawContext c, float x, float y, float radius, int color, float scale) {
        int r = Math.max(1, Math.round(radius * scale)), ix = Math.round(x), iy = Math.round(y);
        c.fill(ix - r, iy - r, ix + r + 1, iy + r + 1, color);
    }

    private static float smoothstep(float x) { x = Math.max(0f, Math.min(1f, x)); return x * x * (3f - 2f * x); }
    private static float easeInOut(float x) { x = Math.max(0f, Math.min(1f, x)); return x < .5f ? 2f*x*x : 1f - (float)Math.pow(-2f*x+2f, 2)/2f; }

    private static boolean enabled(String name) {
        return Metzclient.MODULES.all().stream().anyMatch(m -> m.name().equals(name) && m.enabled());
    }
}
