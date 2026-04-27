package com.yourname.bonemealauto.core;

public class TickTimer {
    private int tickCounter = 0;
    private int targetTick = 0;
    private Runnable callback = null;
    private boolean active = false;

    public void tick() {
        if (!active) return;
        tickCounter++;
        if (tickCounter >= targetTick && callback != null) {
            active = false;
            Runnable cb = callback;
            callback = null;
            try {
                cb.run();
            } catch (Exception e) {
                com.yourname.bonemealauto.FarmLoopManager.onError("TickTimer callback failed: " + e.getMessage());
            }
        }
    }

    public void wait(int ticks, Runnable onDone) {
        this.tickCounter = 0;
        this.targetTick = Math.max(1, ticks);
        this.callback = onDone;
        this.active = true;
    }

    public void cancel() {
        active = false;
        callback = null;
    }

    public boolean isActive() {
        return active;
    }
}
