package dev.metzclient.schematic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;

/** Loads and validates a blueprint; placement remains manual/survival-safe. */
public final class BlueprintPreview {
    private BlueprintPreview() {}

    public static boolean loadAndReport(String filename) {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            Blueprint blueprint = new BlueprintManager().load(filename);
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§bMetzclient §7Loaded blueprint §f" + blueprint.name()
                    + " §7(" + blueprint.width() + "×" + blueprint.height() + "×" + blueprint.length()
                    + ", " + blueprint.expectedBlocks() + " blocks)."), false);
            }
            return true;
        } catch (IOException | RuntimeException ex) {
            if (client.player != null) client.player.sendMessage(Text.literal("§cMetzclient blueprint error: " + ex.getMessage()), false);
            return false;
        }
    }
}
