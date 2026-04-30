package com.example.bonemeal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class BoneMealAutoMod implements ClientModInitializer {
    private static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bonemeal.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.bonemeal"
        ));

        FarmLoopManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                FarmLoopManager.toggle();
                client.player.sendMessage(Text.literal("BoneMeal Auto: " + (FarmLoopManager.isActive() ? "§aON" : "§cOFF")), true);
            }
        });
    }
}
