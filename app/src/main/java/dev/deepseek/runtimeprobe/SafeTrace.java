package dev.deepseek.runtimeprobe;

import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;

import de.robv.android.xposed.XposedBridge;

final class SafeTrace {
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private SafeTrace() {}

    static void event(String name, String metadata) {
        XposedBridge.log("[DSRP][" + SEQUENCE.incrementAndGet() + "][" + name + "] " + metadata);
    }

    static void failure(String name, Throwable throwable) {
        event(name, "exception_type=" + (throwable == null ? "null" : throwable.getClass().getName()));
    }

    static String argumentTypes(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (Object arg : args) joiner.add(arg == null ? "null" : arg.getClass().getName());
        return joiner.toString();
    }
}
