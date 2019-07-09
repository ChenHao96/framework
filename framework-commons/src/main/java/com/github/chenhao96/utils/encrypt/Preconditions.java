package com.github.chenhao96.utils.encrypt;

public class Preconditions {
    public static void checkArgument(boolean result, String msg) {
        if (!result) throw new IllegalArgumentException(msg);
    }
}
