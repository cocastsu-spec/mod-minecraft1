package com.yourname.bonemealauto.controller;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Set;

public class ItemDropper {
    private static final Set<Item> SEEDS = Set.of(
        Items.WHEAT_SEEDS,
        Items.MELON_SEEDS,
        Items.PUMPKIN_SEEDS,
        Items.BEETROOT_SEEDS,
        Items.TORCHFLOWER_SEEDS,
        Items.PITCHER_POD
    );

    /**
     * Drop all seed items from main inventory (never touches offhand)
     */
    public void dropAllSeeds(ClientPlayerEntity player) {
        var inv = player.getInventory();
        // Only iterate main inventory (slots 0–35), never offhand
        for (int i = 0; i < inv.main.size(); i++) {
            ItemStack stack = inv.main.get(i);
            if (!stack.isEmpty() && SEEDS.contains(stack.getItem())) {
                // Set count to 0 effectively removes from inv client-side;
                // actual drop happens via packet
                player.dropItem(stack, true);
                inv.main.set(i, ItemStack.EMPTY);
            }
        }
    }

    public boolean hasAnySeeds(ClientPlayerEntity player) {
        var inv = player.getInventory();
        for (var stack : inv.main) {
            if (!stack.isEmpty() && SEEDS.contains(stack.getItem())) return true;
        }
        return false;
    }
}
