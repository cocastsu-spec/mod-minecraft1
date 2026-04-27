package com.yourname.bonemealauto;

import com.yourname.bonemealauto.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BoneMeal AutoFarm — Fabric Client Mod
 * Entrypoint: runs on client side only
 *
 * Keybind: J (default) — toggle the auto-farm on/off
 */
public class BoneMealAutoMod implements ClientModInitializer {
    public static final String MOD_ID = "bonemealauto";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[BoneMealAuto] Initializing v4.0.0...");

        // Load config from disk
        ModConfig.load();

        // Register keybind: J to toggle
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bonemealauto.toggle",          // translation key
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,                    // default: J
            "category.bonemealauto"             // category in controls menu
        ));

        // Register tick event — runs every client tick (20/sec)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle keybind press
            while (toggleKey.wasPressed()) {
                FarmLoopManager.toggle();
            }
            // Tick the state machine
            FarmLoopManager.tick();
        });

        // Register screen open event — event-driven GUI detection
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            FarmLoopManager.onScreenOpened(screen);
        });

        LOGGER.info("[BoneMealAuto] Ready. Press J in-game to toggle.");
    }
}
