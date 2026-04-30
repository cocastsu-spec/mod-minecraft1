package com.example.bonemeal.core;

public class TickTimer {
    private int ticks = 0;
    private Runnable action;

    public void set(int ticks, Runnable action) {
        this.ticks = ticks;
        this.action = action;
    }

    public void tick() {
        if (ticks > 0) {
            ticks--;
            if (ticks == 0 && action != null) {
                action.run();
            }
        }
    }
}
