package dev.metzclient.ui;

import dev.metzclient.Metzclient;
import dev.metzclient.module.ClientModule;
import dev.metzclient.module.ModuleCategory;
import dev.metzclient.waypoint.Waypoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MetzScreen extends Screen {
    private static final int BG = 0xE9101218, PANEL = 0xE91B1F29, HEADER = 0xFF252B38;
    private static final int CYAN = 0xFF00D7F5, BLUE = 0xFF078BFF, TEXT = 0xFFF1F5FA, MUTED = 0xFF9BA8B8;
    private final List<ModuleCategory> categories = List.of(
        ModuleCategory.COMBAT, ModuleCategory.MOVEMENT, ModuleCategory.WORLD,
        ModuleCategory.RENDER, ModuleCategory.CLIENT
    );
    private final List<RowHit> rowHits = new ArrayList<>();
    private TextFieldWidget search;
    private int tab = 0;
    private int animationTick = 0;

    public MetzScreen() { super(Text.literal("Metzclient")); }

    @Override protected void init() {
        search = new TextFieldWidget(textRenderer, 18, 28, Math.min(210, width - 40), 18, Text.literal("Search modules"));
        search.setPlaceholder(Text.literal("Search modules..."));
        search.setDrawsBackground(false);
        search.setMaxLength(64);
        addDrawableChild(search);
    }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        animationTick++;
        ctx.fill(0, 0, width, height, 0x77040810);
        int margin = 12, gap = 7, columns = 5;
        int panelWidth = Math.max(100, (width - margin * 2 - gap * (columns - 1)) / columns);
        int top = 54, panelHeight = Math.max(100, height - top - 22);
        ctx.fill(margin, 8, width - margin, 47, BG);
        ctx.fill(margin, 8, width - margin, 10, CYAN);
        ctx.drawText(textRenderer, "METZCLIENT", margin + 10, 15, TEXT, true);
        ctx.drawText(textRenderer, "FABRIC 1.21.1  •  RIGHT SHIFT", margin + 10, 30, MUTED, false);
        drawLogoMark(ctx, width - margin - 34, 12, 27);

        rowHits.clear();
        String query = search == null ? "" : search.getText().toLowerCase(Locale.ROOT);
        for (int i = 0; i < categories.size(); i++) {
            ModuleCategory category = categories.get(i);
            int x = margin + i * (panelWidth + gap);
            ctx.fill(x, top, x + panelWidth, top + panelHeight, PANEL);
            ctx.fill(x, top, x + panelWidth, top + 21, HEADER);
            ctx.fill(x, top, x + panelWidth, top + 2, i % 2 == 0 ? CYAN : BLUE);
            ctx.drawText(textRenderer, category.label(), x + 6, top + 7, CYAN, true);
            List<ClientModule> modules = Metzclient.MODULES.all().stream()
                .filter(m -> belongsToColumn(m, category))
                .filter(m -> query.isBlank() || m.name().toLowerCase(Locale.ROOT).contains(query)
                    || m.description().toLowerCase(Locale.ROOT).contains(query)).toList();
            int y = top + 26;
            for (ClientModule module : modules) {
                if (y + 18 > top + panelHeight - 5) break;
                boolean enabled = module.enabled();
                boolean hovered = mouseX >= x + 4 && mouseX <= x + panelWidth - 4 && mouseY >= y - 2 && mouseY < y + 15;
                ctx.fill(x + 4, y - 2, x + panelWidth - 4, y + 15, hovered ? 0xFF30394A : 0xFF202530);
                ctx.drawText(textRenderer, fit(module.name(), panelWidth - 32), x + 7, y + 2, enabled ? TEXT : MUTED, false);
                int tx = x + panelWidth - 21;
                ctx.fill(tx, y + 3, tx + 13, y + 10, enabled ? 0xFF006C8C : 0xFF444B58);
                int knob = enabled ? tx + 7 : tx + 1;
                ctx.fill(knob, y + 4, knob + 5, y + 9, enabled ? CYAN : 0xFF8A95A4);
                rowHits.add(new RowHit(x + 4, y - 2, x + panelWidth - 4, y + 15, module));
                y += 19;
            }
        }

        int footerY = height - 16;
        ctx.drawText(textRenderer, "K  SAVE WAYPOINT", margin + 2, footerY, MUTED, false);
        List<Waypoint> points = Metzclient.WAYPOINTS.all();
        String wp = points.isEmpty() ? "WAYPOINTS: NONE — PRESS K TO SAVE YOUR POSITION" :
            "WAYPOINTS: " + points.size() + "  •  LAST: " + points.get(points.size() - 1).x() + ", " +
                points.get(points.size() - 1).y() + ", " + points.get(points.size() - 1).z();
        ctx.drawText(textRenderer, fit(wp, width / 2), width / 2, footerY, CYAN, false);
        super.render(ctx, mouseX, mouseY, delta);
    }


    private boolean belongsToColumn(ClientModule module, ModuleCategory column) {
        return switch (column) {
            case COMBAT -> module.category() == ModuleCategory.COMBAT;
            case MOVEMENT -> module.category() == ModuleCategory.MOVEMENT || module.category() == ModuleCategory.PLAYER;
            case WORLD -> module.category() == ModuleCategory.WORLD;
            case RENDER -> module.category() == ModuleCategory.RENDER;
            case CLIENT -> module.category() == ModuleCategory.CLIENT || module.category() == ModuleCategory.UTILITY;
            default -> false;
        };
    }

    private String fit(String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) return value;
        String out = value;
        while (!out.isEmpty() && textRenderer.getWidth(out + "...") > maxWidth) out = out.substring(0, out.length() - 1);
        return out + "...";
    }

    private void drawLogoMark(DrawContext ctx, int x, int y, int size) {
        ctx.fill(x, y, x + size, y + size, 0xFF101722);
        ctx.fill(x + 2, y + 2, x + size - 2, y + size - 2, 0xFF050A12);
        ctx.drawText(textRenderer, "MC", x + 3, y + 8, CYAN, true);
        ctx.fill(x + 2, y + size - 3, x + size - 2, y + size - 2, BLUE);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) for (RowHit hit : rowHits) {
            if (mouseX >= hit.x1 && mouseX <= hit.x2 && mouseY >= hit.y1 && mouseY <= hit.y2) {
                hit.module.toggle(); return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean shouldPause() { return false; }
    private record RowHit(int x1, int y1, int x2, int y2, ClientModule module) {}
}
