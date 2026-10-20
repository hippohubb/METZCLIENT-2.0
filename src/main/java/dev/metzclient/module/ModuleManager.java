package dev.metzclient.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.MinecraftClient;

public final class ModuleManager {
    private final List<ClientModule> modules = new ArrayList<>();
    public void add(ClientModule module) { modules.add(module); }
    public List<ClientModule> all() { return Collections.unmodifiableList(modules); }
    public List<ClientModule> byCategory(ModuleCategory category) { return modules.stream().filter(m -> m.category() == category).toList(); }
    public void tick(MinecraftClient client) { for (ClientModule module : modules) if (module.enabled()) module.tick(client); }

    public void registerDefaults() {
        if (!modules.isEmpty()) return;
        String[][] defs = {
            {"Sprint Assist","Movement"}, {"Toggle Sneak","Movement"}, {"Safe Walk","Movement"},
            {"Coordinates HUD","Render"}, {"Storage ESP","Render"}, {"Armor Status","Utility"}, {"Effect Timers","Utility"},
            {"Waypoint Manager","World"}, {"Base Finder","World"}, {"Base Markers","World"},
            {"Schematic Preview","World"}, {"Auto Schematic Builder","World"}, {"Material Checklist","World"},
            {"Path Planner","World"}, {"Auto Miner","World"}, {"Mining Alerts","World"},
            {"Inventory Helper","Player"}, {"Item Highlights","Render"}, {"Keystrokes","Client"},
            {"CPS Counter","Client"}, {"Theme Editor","Client"}, {"Menu Animations","Client"},
            {"UI Sounds","Client"}, {"Profile Manager","Client"}, {"Search Modules","Client"},
            {"Butterfly Knife Cosmetic","Client"}, {"Bayonet Cosmetic","Client"},
            {"Inspect Animation","Client"}, {"Draw Animation","Client"}, {"Idle Animation","Client"},
            {"Fullbright","Render"}, {"Entity Labels","Render"}, {"Block Outline","Render"},
            {"Waypoint Beams","Render"}, {"Screenshot Mode","Utility"}, {"Ping Display","Utility"},
            {"FPS Display","Utility"}, {"Clock HUD","Utility"}, {"Compass HUD","Utility"},
            {"Auto Tool Suggestion","Player"}, {"Durability Warnings","Player"}, {"Food Warnings","Player"},
            {"Chat Timestamp","Client"}, {"Chat Filter","Client"}, {"Compact Notifications","Client"},
            {"Crosshair Editor","Render"}, {"Color Presets","Client"}, {"Accent Gradient","Client"},
            {"Panel Blur Style","Client"}, {"Rounded Panel Style","Client"}, {"Low Motion Mode","Client"},
            {"Toggle Animation","Client"}, {"Module Favorites","Client"}, {"Category Pins","Client"},
            {"Quick Search","Client"}, {"Config Export","Client"}, {"Config Import","Client"},
            {"HUD Layout","Client"}, {"HUD Scale","Client"}, {"Waypoint Labels","World"},
            {"Waypoint Distance","World"}, {"Dimension Tags","World"}, {"Last Position","World"},
            {"Spawn Marker","World"}, {"Build Grid","World"}, {"Blueprint Rotation","World"},
            {"Blueprint Mirror","World"}, {"Blueprint Layer View","World"}, {"Blueprint Progress","World"},
            {"Required Materials","World"}, {"Missing Block Counter","World"}, {"Placement Assist","World"},
            {"Mining Route Preview","World"}, {"Tool Durability Check","World"}, {"Ore Reminder","World"},
            {"Block Interaction Range","Player"}, {"Mouse Sensitivity Preset","Player"}, {"Attack Indicator","Render"},
            {"Hitbox Visualizer (Practice)","Render"}, {"Particle Settings","Render"}, {"Weather Overlay","Render"},
            {"Biome Label","Render"}, {"Direction HUD","Render"}, {"Session Timer","Utility"},
            {"World Time","Utility"}, {"Memory Monitor","Utility"}, {"Reconnect Helper","Utility"},
            {"Screenshot Gallery","Utility"}, {"Menu Background Dim","Client"}, {"Menu Fade","Client"},
            {"Menu Slide","Client"}, {"Accent Pulse","Client"}, {"Keybind Editor","Client"},
            {"Module Descriptions","Client"}, {"About Metzclient","Client"}, {"Reset Settings","Client"},
            {"Developer Overlay","Client"}, {"Debug Log","Utility"}, {"Safe Mode","Client"},
            {"Update Notice","Client"}, {"Welcome Screen","Client"}
        };
        for (String[] d : defs) {
            ModuleCategory c = switch (d[1]) {
                case "Movement" -> ModuleCategory.MOVEMENT;
                case "World" -> ModuleCategory.WORLD;
                case "Render" -> ModuleCategory.RENDER;
                case "Player" -> ModuleCategory.PLAYER;
                case "Utility" -> ModuleCategory.UTILITY;
                default -> ModuleCategory.CLIENT;
            };
            add(new ClientModule(d[0], "Metzclient " + d[0] + " module", c));
        }
    }
}
