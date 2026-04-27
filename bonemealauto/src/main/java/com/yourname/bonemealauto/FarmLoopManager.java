package com.yourname.bonemealauto;

import com.yourname.bonemealauto.config.ModConfig;
import com.yourname.bonemealauto.controller.*;
import com.yourname.bonemealauto.core.State;
import com.yourname.bonemealauto.core.TickTimer;
import com.yourname.bonemealauto.core.WatchdogTimer;
import com.yourname.bonemealauto.gui.GuiController;
import com.yourname.bonemealauto.scanner.InventoryScanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central state machine for BoneMeal AutoFarm.
 *
 * FULL LOOP:
 * IDLE → (bone_meal == 0) → DROP_SEEDS → POSITIONING
 *   → SHOP (×4 loops, each: 7 GUI menus + CRAFT)
 *   → WAIT_BEFORE_KHO → KHO (2 GUI menus)
 *   → IDLE
 */
public class FarmLoopManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("bonemealauto");

    private static boolean enabled = false;
    private static State currentState = State.IDLE;

    // Components
    private static final TickTimer timer = new TickTimer();
    private static final WatchdogTimer watchdog = new WatchdogTimer();
    private static final InventoryScanner scanner = new InventoryScanner();
    private static final LookController lookCtrl = new LookController();
    private static final ItemDropper dropper = new ItemDropper();
    private static final MovementController moveCtrl = new MovementController();
    private static final ChatCommandSender chatSender = new ChatCommandSender();
    private static final CraftingHandler crafter = new CraftingHandler(timer);
    private static final GuiController guiCtrl = new GuiController(timer);

    // State timeout tracking
    private static int stateTimeout = 0;
    private static int retryAttempt = 0;
    private static final int MAX_RETRIES = 2;

    // ── Toggle ────────────────────────────────────────────────────────────
    public static void toggle() {
        enabled = !enabled;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (enabled) {
                client.player.sendMessage(
                    net.minecraft.text.Text.literal("§a[BoneMealAuto] §fEnabled — monitoring bone meal..."), false
                );
                scanner.forceScan();
            } else {
                client.player.sendMessage(
                    net.minecraft.text.Text.literal("§c[BoneMealAuto] §fDisabled."), false
                );
                forceReset("User disabled mod");
            }
        }
    }

    public static boolean isEnabled() { return enabled; }
    public static State getState() { return currentState; }

    // ── Main tick (called every client tick) ──────────────────────────────
    public static void tick() {
        if (!enabled) return;

        timer.tick();
        watchdog.tick();
        scanner.tick();

        // State timeout enforcement
        if (currentState != State.IDLE) {
            stateTimeout--;
            if (stateTimeout <= 0) {
                retryAttempt++;
                log("State " + currentState + " timed out (attempt " + retryAttempt + ")");
                if (retryAttempt > MAX_RETRIES) {
                    forceReset("State " + currentState + " timed out after max retries");
                } else {
                    retryCurrentState();
                }
            }
        }
    }

    // ── Screen opened event (from BoneMealAutoMod) ────────────────────────
    public static void onScreenOpened(Screen screen) {
        if (!enabled) return;
        guiCtrl.onScreenOpened(screen);
    }

    // ── State transitions ─────────────────────────────────────────────────
    public static void setState(State newState) {
        log("State: " + currentState + " → " + newState);
        currentState = newState;
        watchdog.onStateChange(newState);
        retryAttempt = 0;

        MinecraftClient client = MinecraftClient.getInstance();

        switch (newState) {
            case IDLE -> {
                stateTimeout = 0;
                guiCtrl.resetShopLoopCount();
            }

            case DROP_SEEDS -> {
                stateTimeout = ModConfig.get().DROP_SEEDS_TIMEOUT;
                if (client.player != null) {
                    lookCtrl.lookDown(client.player);
                    dropper.dropAllSeeds(client.player);
                    timer.wait(10, () -> {
                        if (client.player != null) lookCtrl.restoreLook(client.player);
                        setState(State.POSITIONING);
                    });
                } else {
                    forceReset("No player in DROP_SEEDS");
                }
            }

            case POSITIONING -> {
                stateTimeout = ModConfig.get().POSITIONING_TIMEOUT;
                moveCtrl.startPositioning(client, timer);
            }

            case SHOP -> {
                stateTimeout = ModConfig.get().SHOP_TIMEOUT;
                guiCtrl.beginShop();
            }

            case CRAFT -> {
                stateTimeout = ModConfig.get().CRAFT_TIMEOUT;
                crafter.startCrafting(client);
            }

            case WAIT_BEFORE_KHO -> {
                stateTimeout = ModConfig.get().WAIT_BEFORE_KHO;
                timer.wait(ModConfig.get().WAIT_BEFORE_KHO, () -> setState(State.KHO));
            }

            case KHO -> {
                stateTimeout = ModConfig.get().KHO_TIMEOUT;
                guiCtrl.beginKhoPrestep();
            }
        }
    }

    // ── Callbacks from sub-systems ────────────────────────────────────────

    /** Called by MovementController when positioning is done */
    public static void onMovementDone() {
        if (currentState != State.POSITIONING) return;
        setState(State.SHOP);
    }

    /** Called by CraftingHandler when crafting is done */
    public static void onCraftingDone() {
        if (currentState != State.CRAFT) return;
        // Check if we need more shop loops
        if (guiCtrl.getShopLoopCount() < 4) {
            setState(State.SHOP);
        } else {
            setState(State.WAIT_BEFORE_KHO);
        }
    }

    /** Called by GuiController on success */
    public static void onGuiClickDone() {
        retryAttempt = 0;
    }

    public static void retryCurrentState() {
        State s = currentState;
        currentState = State.IDLE; // briefly reset so setState runs fully
        setState(s);
    }

    public static void forceReset(String reason) {
        LOGGER.warn("[BoneMealAuto] FORCE RESET: {}", reason);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) {
            client.setScreen(null);
        }
        moveCtrl.stopAll(client);
        timer.cancel();
        guiCtrl.setWaitingForGui(false);
        guiCtrl.resetShopLoopCount();
        currentState = State.IDLE;
        stateTimeout = 0;
        retryAttempt = 0;
        watchdog.onStateChange(State.IDLE);
    }

    // ── Logging helpers ───────────────────────────────────────────────────
    public static void log(String message) {
        if (!ModConfig.get().ENABLE_LOGGING) return;
        LOGGER.info("[BoneMealAuto] {}", message);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                net.minecraft.text.Text.literal("§7[BoneMealAuto] §f" + message), false
            );
        }
    }

    public static void onError(String message) {
        LOGGER.error("[BoneMealAuto] ERROR: {}", message);
    }

    public static boolean isWaitingForGui() {
        return guiCtrl.isWaitingForGui();
    }

    public static TickTimer getTimer() { return timer; }
}
