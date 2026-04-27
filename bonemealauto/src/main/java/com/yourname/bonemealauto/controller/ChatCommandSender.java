package com.yourname.bonemealauto.controller;

import net.minecraft.client.MinecraftClient;

public class ChatCommandSender {

    /**
     * Send a slash command to the server.
     * Pass command WITHOUT the leading slash.
     * e.g. sendCommand(client, "shop") → sends /shop
     */
    public void sendCommand(MinecraftClient client, String command) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        client.player.networkHandler.sendChatCommand(command);
    }

    public void sendChat(MinecraftClient client, String message) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        client.player.networkHandler.sendChatMessage(message);
    }
}
