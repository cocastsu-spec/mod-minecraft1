package com.example.bonemeal;

import net.minecraft.client.MinecraftClient;

public class FarmLoopManager {

    private static boolean active = false;

    public static void toggle() {
        active = !active;
    }

    public static boolean isActive() {
        return active;
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return;

        // 👉 logic farm của bạn viết ở đây
        // ví dụ test:
        // client.player.jump();
    }
}
