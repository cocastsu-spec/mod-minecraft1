package com.yourname.bonemealauto.util;

import java.util.Random;

public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static int randomDelay(int min, int max) {
        if (max <= min) return min;
        return min + RANDOM.nextInt(max - min + 1);
    }

    public static int nextInt(int bound) {
        return RANDOM.nextInt(Math.max(1, bound));
    }
}
