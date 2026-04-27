package com.yourname.bonemealauto.scanner;

import com.yourname.bonemealauto.FarmLoopManager;
import com.yourname.bonemealauto.config.ModConfig;
import com.yourname.bonemealauto.core.State;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class InventoryScanner {
    private int scanCooldown = 0;

    public void tick() {
        if (!FarmLoopManager.isEnabled()) return;
        if (FarmLoopManager.getState() != State.IDLE) return;

        scanCooldown--;
        if (scanCooldown <= 0) {
            scanCooldown = ModConfig.get().SCAN_INTERVAL;
            if (isBoneMealEmpty()) {
                FarmLoopManager.log("Bone meal empty — starting restock loop");
                FarmLoopManager.setState(State.DROP_SEEDS);
            }
        }
    }

    /** Force immediate scan (e.g., when manually triggered) */
    public void forceScan() {
        scanCooldown = 0;
    }

    private boolean isBoneMealEmpty() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        var inv = client.player.getInventory();
        for (var stack : inv.main) {
            if (!stack.isEmpty() && stack.getItem() == Items.BONE_MEAL) {
                return false;
            }
        }
        return true;
    }

    /** Scan inventory for a specific item, return first slot index or -1 */
    public static int findItemSlot(Item item) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return -1;
        var inv = client.player.getInventory();
        for (int i = 0; i < inv.main.size(); i++) {
            if (!inv.main.get(i).isEmpty() && inv.main.get(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    /** Check if inventory has a wheat item */
    public static boolean hasWheat() {
        return findItemSlot(Items.WHEAT) != -1;
    }
}
