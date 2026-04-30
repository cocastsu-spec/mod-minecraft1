package com.example.bonemeal.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class GuiController {
    
    public static void clickSlot(int slotIndex) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen == null) return;
        
        int syncId = 0;
        if (client.currentScreen instanceof GenericContainerScreen gcs) {
            syncId = gcs.getScreenHandler().syncId;
        } else if (client.currentScreen instanceof InventoryScreen is) {
            syncId = is.getScreenHandler().syncId;
        }
        
        client.interactionManager.clickSlot(syncId, slotIndex, 0, SlotActionType.PICKUP, client.player);
    }

    public static int findWheatInInventory() {
        var inv = MinecraftClient.getInstance().player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isOf(Items.WHEAT)) return i;
        }
        return -1;
    }

    public static boolean isWheatInMenu() {
        var client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof GenericContainerScreen gcs) {
            var slots = gcs.getScreenHandler().slots;
            for (int i = 0; i < 54; i++) {
                if (slots.get(i).getStack().getName().getString().toLowerCase().contains("wheat")) return true;
                if (slots.get(i).getStack().isOf(Items.WHEAT)) return true;
            }
        }
        return false;
    }
}
