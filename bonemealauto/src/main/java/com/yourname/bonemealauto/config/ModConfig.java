package com.yourname.bonemealauto.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("bonemealauto");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
        FabricLoader.getInstance().getConfigDir().toFile(),
        "bonemealauto.json"
    );

    private static ModConfig INSTANCE = new ModConfig();

    // ── SHOP GUI slots (0-indexed absolute slot in the GUI handler) ──
    // Menu 1 (player inventory): slot at row 2, position 8  → fill via config
    public int SHOP_MENU1_SLOT = 16;   // row2 col8 in player inv (27-slot = row2*9+col7)
    // Menu 2–5 (double chest): slot at row 6, position 7
    public int SHOP_MENU2_SLOT = 52;
    public int SHOP_MENU3_SLOT = 52;
    public int SHOP_MENU4_SLOT = 52;
    public int SHOP_MENU5_SLOT = 52;
    // Menu 6 (double chest): slot at row 4, position 6
    public int SHOP_MENU6_SLOT = 32;
    // Menu 7 (double chest): slot at row 3, position 3
    public int SHOP_MENU7_SLOT = 20;

    // ── KHO GUI slots ──
    // Menu 1 (double chest): slot at row 2, position 8 OR wheat item
    public int KHO_MENU1_SLOT = 16;
    public boolean KHO_PREFER_WHEAT_ITEM = true; // if true, scan for Wheat item instead
    // Menu 2 (player inventory): slot at row 3, position 4
    public int KHO_MENU2_SLOT = 22;

    // ── Timing (ticks, 20 ticks = 1 second) ──
    public int DROP_SEEDS_TIMEOUT = 60;
    public int POSITIONING_TIMEOUT = 100;
    public int SHOP_TIMEOUT = 600;
    public int CRAFT_TIMEOUT = 200;
    public int KHO_TIMEOUT = 400;
    public int WAIT_BEFORE_KHO = 400;
    public int SCAN_INTERVAL = 7200;
    public int WATCHDOG_TICKS = 6000;
    public int MAX_CRAFT_LOOPS = 256;
    public int MAX_RETRY_ATTEMPTS = 2;
    public int RANDOM_DELAY_MIN = 1;
    public int RANDOM_DELAY_MAX = 3;

    // ── Features ──
    public boolean DYNAMIC_SLOT_DETECTION = true;
    public boolean AUTO_LOGIN = false;
    public String LOGIN_COMMAND = "";
    public boolean ACCEPT_RESOURCE_PACK = true;
    public boolean ENABLE_LOGGING = true;

    // ── Keybind (default key "j") ──
    public String TOGGLE_KEY = "key.keyboard.j";

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    LOGGER.info("[BoneMealAuto] Config loaded from {}", CONFIG_FILE.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.error("[BoneMealAuto] Failed to load config: {}", e.getMessage());
                save(new ModConfig());
            }
        } else {
            LOGGER.info("[BoneMealAuto] No config found, creating default at {}", CONFIG_FILE.getAbsolutePath());
            save(new ModConfig());
        }
    }

    public static void save(ModConfig config) {
        INSTANCE = config;
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
            LOGGER.info("[BoneMealAuto] Config saved.");
        } catch (IOException e) {
            LOGGER.error("[BoneMealAuto] Failed to save config: {}", e.getMessage());
        }
    }
}
