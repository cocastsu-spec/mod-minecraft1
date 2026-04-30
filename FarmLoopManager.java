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

        // ❗ Fix crash: đảm bảo không null
        if (client == null || client.player == null || client.world == null) return;

        // ====== LOGIC TEST (không crash) ======
        // Bạn có thể thay bằng auto farm thật sau

        // ví dụ đơn giản:
        // mỗi tick nhảy nhẹ (test mod chạy)
        // client.player.jump();

    }
}
