package com.yourname.bonemealauto.controller;

import com.yourname.bonemealauto.FarmLoopManager;
import com.yourname.bonemealauto.config.ModConfig;
import com.yourname.bonemealauto.core.TickTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class CraftingHandler {
    private int craftLoopCount = 0;
    private TickTimer timer;

    public CraftingHandler(TickTimer timer) {
        this.timer = timer;
    }

    public void startCrafting(MinecraftClient client) {
        craftLoopCount = 0;
        if (!hasEnoughBone(client)) {
            FarmLoopManager.log("No bones to craft, skipping craft phase");
            FarmLoopManager.onCraftingDone();
            return;
        }
        openInventory(client);
        // Small delay to let inventory screen open
        timer.wait(2, () -> craftBone(client));
    }

    private void craftBone(MinecraftClient client) {
        craftLoopCount++;

        // Guard: max loops
        if (craftLoopCount > ModConfig.get().MAX_CRAFT_LOOPS) {
            FarmLoopManager.log("Craft loop limit reached (" + craftLoopCount + ")");
            onDone(client);
            return;
        }

        if (client.player == null) {
            onDone(client);
            return;
        }

        var inv = client.player.getInventory();

        // Guard: no more bones
        int boneSlot = findSlot(client, Items.BONE);
        if (boneSlot == -1) {
            onDone(client);
            return;
        }

        // Guard: inventory too full (35/36 filled)
        long usedSlots = inv.main.stream().filter(s -> !s.isEmpty()).count();
        if (usedSlots >= 35) {
            FarmLoopManager.log("Inventory full, stopping craft");
            onDone(client);
            return;
        }

        // Perform craft: bone → 3 bone meal
        // bone is at boneSlot (inventory index) → handler slot = boneSlot + 9
        PlayerScreenHandler handler = client.player.playerScreenHandler;
        var manager = client.interactionManager;
        if (manager == null) { onDone(client); return; }

        int handlerSlot = boneSlot + 9; // offset for hotbar vs handler mapping

        // Pick up bone
        manager.clickSlot(handler.syncId, handlerSlot, 0, SlotActionType.PICKUP, client.player);
        // Place in crafting slot 1
        manager.clickSlot(handler.syncId, 1, 0, SlotActionType.PICKUP, client.player);
        // Shift-click crafting output (slot 0) to collect bone meal
        manager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, client.player);

        // Loop again next tick
        timer.wait(1, () -> craftBone(client));
    }

    private void onDone(MinecraftClient client) {
        closeInventory(client);
        craftLoopCount = 0;
        // Small delay before signaling done
        timer.wait(2, () -> FarmLoopManager.onCraftingDone());
    }

    private int findSlot(MinecraftClient client, Item item) {
        var inv = client.player.getInventory();
        for (int i = 0; i < inv.main.size(); i++) {
            if (!inv.main.get(i).isEmpty() && inv.main.get(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasEnoughBone(MinecraftClient client) {
        return client.player != null && findSlot(client, Items.BONE) != -1;
    }

    private void openInventory(MinecraftClient client) {
        client.setScreen(new InventoryScreen(client.player));
    }

    private void closeInventory(MinecraftClient client) {
        if (client.currentScreen instanceof InventoryScreen) {
            client.setScreen(null);
        }
    }
}
