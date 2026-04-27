package com.yourname.bonemealauto.controller;

import com.yourname.bonemealauto.FarmLoopManager;
import com.yourname.bonemealauto.core.TickTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

public class MovementController {

    public void startPositioning(MinecraftClient client, TickTimer timer) {
        GameOptions opts = client.options;
        opts.forwardKey.setPressed(true);
        opts.jumpKey.setPressed(true);

        // Move forward+jump for 50 ticks (~2.5 sec, ~10-11 blocks)
        timer.wait(50, () -> {
            opts.forwardKey.setPressed(false);
            opts.jumpKey.setPressed(false);
            FarmLoopManager.onMovementDone();
        });
    }

    public void stopAll(MinecraftClient client) {
        if (client.options == null) return;
        GameOptions opts = client.options;
        opts.forwardKey.setPressed(false);
        opts.backKey.setPressed(false);
        opts.leftKey.setPressed(false);
        opts.rightKey.setPressed(false);
        opts.jumpKey.setPressed(false);
        opts.sneakKey.setPressed(false);
    }
}
