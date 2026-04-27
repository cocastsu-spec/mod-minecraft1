package com.yourname.bonemealauto.gui;

import com.yourname.bonemealauto.FarmLoopManager;
import com.yourname.bonemealauto.config.ModConfig;
import com.yourname.bonemealauto.core.State;
import com.yourname.bonemealauto.core.TickTimer;
import com.yourname.bonemealauto.scanner.InventoryScanner;
import com.yourname.bonemealauto.util.RandomUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Handles all GUI interactions for the /shop and /kho flows.
 *
 * SHOP FLOW (7 menus):
 *   Menu 1: player inventory GUI  → click slot SHOP_MENU1_SLOT
 *   Menu 2–5: double chest GUI    → click slot SHOP_MENU2-5_SLOT (same for all)
 *   Menu 6: double chest GUI      → click slot SHOP_MENU6_SLOT
 *   Menu 7: double chest GUI      → click slot SHOP_MENU7_SLOT
 *   Then craft bones → bone meal
 *   Repeat shop loop 4 times total
 *
 * KHO FLOW (2 menus):
 *   Pre-step: scan inventory for Wheat, if found → open /kho, click Wheat in menu
 *   Menu 1: double chest          → click slot KHO_MENU1_SLOT (or find Wheat by name)
 *   Menu 2: player inventory      → click slot KHO_MENU2_SLOT
 */
public class GuiController {
    private final TickTimer timer;
    private final GuiRetryHandler retryHandler;

    // Shop sub-step tracking (0-6 = menus 1-7)
    private int shopStep = 0;
    // How many full shop loops done (0-3, so 4 total)
    private int shopLoopCount = 0;

    // Kho sub-step (0 = menu1, 1 = menu2)
    private int khoStep = 0;

    // Used to detect GUI change
    private int lastSyncId = -1;
    private boolean waitingForGui = false;

    public GuiController(TickTimer timer) {
        this.timer = timer;
        this.retryHandler = new GuiRetryHandler(timer);
    }

    // ── Called by FarmLoopManager to start a SHOP cycle ──────────────────
    public void beginShop() {
        shopStep = 0;
        retryHandler.resetRetry();
        MinecraftClient client = MinecraftClient.getInstance();
        FarmLoopManager.log("SHOP loop #" + (shopLoopCount + 1) + " — sending /shop");
        new com.yourname.bonemealauto.controller.ChatCommandSender().sendCommand(client, "shop");
        waitingForGui = true;
        // GUI will be handled by onScreenOpened()
    }

    // ── Called by tick loop when a new screen is detected ────────────────
    public void onScreenOpened(Screen screen) {
        if (!waitingForGui) return;
        if (!FarmLoopManager.isEnabled()) return;

        State state = FarmLoopManager.getState();

        if (state == State.SHOP) {
            handleShopScreen(screen);
        } else if (state == State.KHO) {
            handleKhoScreen(screen);
        }
    }

    // ── SHOP screen handler ───────────────────────────────────────────────
    private void handleShopScreen(Screen screen) {
        int targetSlot = getShopSlotForStep(shopStep);
        boolean isPlayerInvScreen = (screen instanceof InventoryScreen);
        boolean isChestScreen = (screen instanceof GenericContainerScreen);

        // Step 0 expects player inventory; steps 1-6 expect chest
        boolean correct = (shopStep == 0 && isPlayerInvScreen) ||
                          (shopStep > 0 && isChestScreen);

        if (!correct) {
            FarmLoopManager.log("Wrong screen type for SHOP step " + shopStep + ", retrying");
            retryHandler.onClickRetry();
            return;
        }

        // Random delay 1–3 ticks before clicking (anti-pattern + sync)
        int delay = RandomUtil.randomDelay(
            ModConfig.get().RANDOM_DELAY_MIN,
            ModConfig.get().RANDOM_DELAY_MAX
        );

        FarmLoopManager.log("SHOP step " + shopStep + " — clicking slot " + targetSlot + " in " + delay + " ticks");

        timer.wait(delay, () -> performClick(screen, targetSlot, () -> {
            retryHandler.setRetryAction(() -> performClick(screen, targetSlot, () -> {}));
            shopStep++;

            if (shopStep > 6) {
                // All 7 shop menus done for this loop — go craft
                waitingForGui = false;
                shopLoopCount++;
                FarmLoopManager.log("SHOP menus done — starting craft");
                FarmLoopManager.setState(State.CRAFT);
            }
            // else wait for next screen to be opened
        }));
    }

    // ── KHO screen handler ────────────────────────────────────────────────
    private void handleKhoScreen(Screen screen) {
        int targetSlot;
        ModConfig cfg = ModConfig.get();

        if (khoStep == 0) {
            // Menu 1: double chest — find Wheat by item name if preferred, else use slot
            if (cfg.KHO_PREFER_WHEAT_ITEM && screen instanceof GenericContainerScreen gcs) {
                targetSlot = findWheatInGui(gcs);
                if (targetSlot == -1) {
                    FarmLoopManager.log("Wheat not found in KHO menu 1, using default slot");
                    targetSlot = cfg.KHO_MENU1_SLOT;
                } else {
                    FarmLoopManager.log("Found Wheat at slot " + targetSlot + " in KHO menu 1");
                }
            } else {
                targetSlot = cfg.KHO_MENU1_SLOT;
            }
        } else {
            // Menu 2: player inventory
            targetSlot = cfg.KHO_MENU2_SLOT;
        }

        int finalSlot = targetSlot;
        int delay = RandomUtil.randomDelay(cfg.RANDOM_DELAY_MIN, cfg.RANDOM_DELAY_MAX);
        FarmLoopManager.log("KHO step " + khoStep + " — clicking slot " + finalSlot);

        timer.wait(delay, () -> performClick(screen, finalSlot, () -> {
            retryHandler.setRetryAction(() -> performClick(screen, finalSlot, () -> {}));
            khoStep++;
            if (khoStep > 1) {
                waitingForGui = false;
                FarmLoopManager.log("KHO done — returning to IDLE");
                FarmLoopManager.setState(State.IDLE);
            }
        }));
    }

    // ── KHO pre-step: check if wheat in player inv, then go /kho ─────────
    public void beginKhoPrestep() {
        khoStep = 0;
        retryHandler.resetRetry();
        MinecraftClient client = MinecraftClient.getInstance();

        if (InventoryScanner.hasWheat()) {
            FarmLoopManager.log("Wheat found in inventory — sending /kho");
            new com.yourname.bonemealauto.controller.ChatCommandSender().sendCommand(client, "kho");
            waitingForGui = true;
        } else {
            FarmLoopManager.log("No wheat in inventory — skipping /kho");
            FarmLoopManager.setState(State.IDLE);
        }
    }

    // ── Generic slot click with success/retry detection ───────────────────
    private void performClick(Screen screen, int slotId, Runnable onSuccess) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) {
            retryHandler.onClickRetry();
            return;
        }

        int syncId;
        if (screen instanceof GenericContainerScreen gcs) {
            syncId = gcs.getScreenHandler().syncId;
        } else if (screen instanceof InventoryScreen is) {
            syncId = is.getScreenHandler().syncId;
        } else {
            retryHandler.onClickRetry();
            return;
        }
        lastSyncId = syncId;

        client.interactionManager.clickSlot(syncId, slotId, 0, SlotActionType.PICKUP, client.player);

        // Wait 2 ticks then verify
        timer.wait(2, () -> {
            if (didGuiChange(lastSyncId)) {
                retryHandler.onClickSuccess();
                onSuccess.run();
            } else {
                retryHandler.onClickRetry();
            }
        });
    }

    private boolean didGuiChange(int oldSyncId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) return true;
        if (client.currentScreen instanceof GenericContainerScreen gcs) {
            return gcs.getScreenHandler().syncId != oldSyncId;
        }
        return true;
    }

    /** Find Wheat item slot in a GenericContainerScreen */
    private int findWheatInGui(GenericContainerScreen screen) {
        var handler = screen.getScreenHandler();
        for (int i = 0; i < handler.slots.size(); i++) {
            var slot = handler.slots.get(i);
            if (!slot.getStack().isEmpty() && slot.getStack().getItem() == Items.WHEAT) {
                return i;
            }
        }
        return -1;
    }

    private int getShopSlotForStep(int step) {
        ModConfig cfg = ModConfig.get();
        return switch (step) {
            case 0 -> cfg.SHOP_MENU1_SLOT;
            case 1 -> cfg.SHOP_MENU2_SLOT;
            case 2 -> cfg.SHOP_MENU3_SLOT;
            case 3 -> cfg.SHOP_MENU4_SLOT;
            case 4 -> cfg.SHOP_MENU5_SLOT;
            case 5 -> cfg.SHOP_MENU6_SLOT;
            case 6 -> cfg.SHOP_MENU7_SLOT;
            default -> cfg.SHOP_MENU7_SLOT;
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    public int getShopLoopCount() { return shopLoopCount; }
    public void resetShopLoopCount() { shopLoopCount = 0; }
    public boolean isWaitingForGui() { return waitingForGui; }
    public void setWaitingForGui(boolean v) { waitingForGui = v; }
}
