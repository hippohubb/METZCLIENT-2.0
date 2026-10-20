package dev.metzclient;

import dev.metzclient.module.ModuleManager;
import dev.metzclient.render.WorldOverlay;
import dev.metzclient.schematic.BlueprintManager;
import dev.metzclient.ui.KnifeCosmeticOverlay;
import dev.metzclient.ui.MetzHud;
import dev.metzclient.ui.MetzScreen;
import dev.metzclient.waypoint.WaypointManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class Metzclient implements ClientModInitializer {
    public static final String MOD_ID = "metzclient";
    public static final ModuleManager MODULES = new ModuleManager();
    public static final WaypointManager WAYPOINTS = new WaypointManager();
    public static final BlueprintManager BLUEPRINTS = new BlueprintManager();
    private static KeyBinding openMenuKey;
    private static KeyBinding addWaypointKey;
    private static KeyBinding inspectKnifeKey;

    @Override
    public void onInitializeClient() {
        MODULES.registerDefaults();
        MetzHud.register();
        KnifeCosmeticOverlay.register();
        WorldOverlay.register();
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.metzclient.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.metzclient"));
        addWaypointKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.metzclient.save_waypoint", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.metzclient"));
        inspectKnifeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.metzclient.inspect_knife", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.metzclient"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new MetzScreen());
                else if (client.currentScreen instanceof MetzScreen) client.setScreen(null);
            }
            while (addWaypointKey.wasPressed()) {
                if (client.player != null && client.world != null) {
                    var p = client.player.getBlockPos();
                    WAYPOINTS.add(new dev.metzclient.waypoint.Waypoint(
                        "Waypoint " + (WAYPOINTS.all().size() + 1),
                        client.world.getRegistryKey().getValue().toString(), p.getX(), p.getY(), p.getZ()));
                    client.player.sendMessage(net.minecraft.text.Text.literal("§bMetzclient §7Saved waypoint at §f" + p.getX() + ", " + p.getY() + ", " + p.getZ()), true);
                }
            }
            while (inspectKnifeKey.wasPressed()) KnifeCosmeticOverlay.inspect();
            MODULES.tick(client);
        });
    }
}
