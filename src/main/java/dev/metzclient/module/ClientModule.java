package dev.metzclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

/**
 * A small real module runtime. Features that need render hooks or assets are intentionally
 * implemented in their own classes instead of being represented as fake behavior here.
 */
public final class ClientModule {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled;
    private boolean wasApplied;
    private int tickCounter;
    private int lastFoodLevel = 20;

    public ClientModule(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String name() { return name; }
    public String description() { return description; }
    public ModuleCategory category() { return category; }
    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void toggle() { enabled = !enabled; }

    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        tickCounter++;

        switch (name) {
            case "Sprint Assist" -> {
                if (enabled && player.forwardSpeed > 0.0F && !player.isSneaking()
                        && player.getHungerManager().getFoodLevel() > 6) player.setSprinting(true);
            }
            case "Toggle Sneak" -> {
                if (!enabled && wasApplied) player.setSneaking(false);
                else if (enabled) player.setSneaking(true);
            }
            case "Safe Walk" -> {
                // Prevents walking off a ledge by temporarily crouching near an unsupported edge.
                if (enabled && client.world != null && player.isOnGround() && player.forwardSpeed != 0) {
                    BlockPos below = player.getBlockPos().down();
                    if (client.world.getBlockState(below).isAir()) player.setSneaking(true);
                    else if (wasApplied) player.setSneaking(false);
                } else if (!enabled && wasApplied) player.setSneaking(false);
            }
            case "Auto Miner" -> {
                // Uses normal client interaction against the block already under the crosshair;
                // it does not bypass reach, tool speed, or server permissions.
                if (enabled && client.world != null && client.interactionManager != null
                        && client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hit
                        && player.squaredDistanceTo(hit.getPos()) <= 36.0) {
                    client.interactionManager.updateBlockBreakingProgress(hit.getBlockPos(), hit.getSide());
                    player.swingHand(Hand.MAIN_HAND);
                }
            }
            case "Food Warnings" -> {
                int food = player.getHungerManager().getFoodLevel();
                if (enabled && tickCounter % 100 == 0 && food <= 6) {
                    player.sendMessage(Text.literal("§bMetzclient §cLow hunger: " + food + "/20"), true);
                }
                lastFoodLevel = food;
            }
            case "Durability Warnings" -> {
                if (enabled && tickCounter % 100 == 0) {
                    ItemStack stack = player.getMainHandStack();
                    if (stack.isDamageable() && stack.getMaxDamage() - stack.getDamage() <= Math.max(8, stack.getMaxDamage() / 10)) {
                        player.sendMessage(Text.literal("§bMetzclient §cHeld item durability is low."), true);
                    }
                }
            }
            case "Auto Tool Suggestion" -> {
                if (enabled && tickCounter % 40 == 0 && client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hit
                        && client.world != null && player.getMainHandStack().isEmpty()) {
                    player.sendMessage(Text.literal("§bMetzclient §7Consider selecting a tool for §f" +
                        client.world.getBlockState(hit.getBlockPos()).getBlock().getName().getString()), true);
                }
            }
            case "Last Position" -> {
                if (enabled && client.world != null) MetzRuntime.lastPosition = player.getBlockPos();
            }
            case "Base Finder" -> {
                // Heuristic only: counts nearby containers in loaded blocks; it cannot find hidden bases or load chunks.
                if (enabled && tickCounter % 200 == 0 && client.world != null) {
                    BlockPos center = player.getBlockPos();
                    int containers = 0;
                    for (BlockPos pos : BlockPos.iterate(center.add(-12, -6, -12), center.add(12, 6, 12))) {
                        var block = client.world.getBlockState(pos).getBlock();
                        if (block == net.minecraft.block.Blocks.CHEST || block == net.minecraft.block.Blocks.TRAPPED_CHEST
                                || block == net.minecraft.block.Blocks.BARREL || block == net.minecraft.block.Blocks.ENDER_CHEST) containers++;
                    }
                    if (containers >= 3) player.sendMessage(Text.literal("§bMetzclient §7Possible storage cluster nearby: §f" + containers + " containers §7within 12 blocks."), true);
                }
            }
            default -> { }
        }
        wasApplied = enabled;
    }

    public static final class MetzRuntime {
        public static BlockPos lastPosition;
        private MetzRuntime() {}
    }
}
