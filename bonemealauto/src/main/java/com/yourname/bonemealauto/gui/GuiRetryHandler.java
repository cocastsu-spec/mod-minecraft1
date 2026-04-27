package com.yourname.bonemealauto.gui;

import com.yourname.bonemealauto.FarmLoopManager;
import com.yourname.bonemealauto.config.ModConfig;
import com.yourname.bonemealauto.core.TickTimer;

public class GuiRetryHandler {
    private int retryCount = 0;
    private TickTimer timer;
    private Runnable retryAction;

    public GuiRetryHandler(TickTimer timer) {
        this.timer = timer;
    }

    public void setRetryAction(Runnable action) {
        this.retryAction = action;
    }

    public void onClickRetry() {
        retryCount++;
        FarmLoopManager.log("GUI click retry #" + retryCount);
        if (retryCount <= ModConfig.get().MAX_RETRY_ATTEMPTS) {
            // Wait 3 ticks then retry
            timer.wait(3, () -> {
                if (retryAction != null) retryAction.run();
            });
        } else {
            FarmLoopManager.log("Max retries reached, retrying current state");
            resetRetry();
            FarmLoopManager.retryCurrentState();
        }
    }

    public void onClickSuccess() {
        resetRetry();
    }

    public void resetRetry() {
        retryCount = 0;
    }
}
