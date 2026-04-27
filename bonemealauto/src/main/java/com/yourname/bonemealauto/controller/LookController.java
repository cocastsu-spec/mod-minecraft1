package com.yourname.bonemealauto.controller;

import net.minecraft.client.network.ClientPlayerEntity;

public class LookController {
    private float savedPitch = 0f;
    private float savedYaw = 0f;

    public void lookDown(ClientPlayerEntity player) {
        savedPitch = player.getPitch();
        savedYaw = player.getYaw();
        player.setPitch(90f); // look straight down
    }

    public void lookUp(ClientPlayerEntity player) {
        savedPitch = player.getPitch();
        savedYaw = player.getYaw();
        player.setPitch(-90f); // look straight up
    }

    public void restoreLook(ClientPlayerEntity player) {
        player.setPitch(savedPitch);
        player.setYaw(savedYaw);
    }
}
