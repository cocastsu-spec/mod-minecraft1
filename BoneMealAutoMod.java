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

        // KHÔNG gọi init ở đây nữa (tránh crash sớm)
        // FarmLoopManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // ❗ Chặn crash: chưa vào world
            if (client.player == null || client.world == null) return;

            // Init muộn (an toàn)
            FarmLoopManager.safeInit(client);

            while (keyBinding.wasPressed()) {
                FarmLoopManager.toggle();

                client.player.sendMessage(
                        Text.literal("BoneMeal Auto: " +
                                (FarmLoopManager.isActive() ? "§aON" : "§cOFF")),
                        true
                );
            }

            // Nếu đang bật thì chạy loop
            if (FarmLoopManager.isActive()) {
                FarmLoopManager.tick(client);
            }
        });
    }
}
