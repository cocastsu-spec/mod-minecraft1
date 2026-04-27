package com.yourname.bonemealauto.core;

import com.yourname.bonemealauto.FarmLoopManager;

public class WatchdogTimer {
    private long lastIdleTick = 0;
    private long currentTick = 0;
    private static final int WATCHDOG_TICKS = 6000; // 5 minutes

    public void onStateChange(State state) {
        if (state == State.IDLE) {
            lastIdleTick = currentTick;
        }
    }

    public void tick() {
        currentTick++;
        if (currentTick - lastIdleTick > WATCHDOG_TICKS) {
            FarmLoopManager.forceReset("WATCHDOG triggered after 5 minutes");
            lastIdleTick = currentTick; // reset so we don't spam
        }
    }
}
