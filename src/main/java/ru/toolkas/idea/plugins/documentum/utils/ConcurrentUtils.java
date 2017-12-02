package ru.toolkas.idea.plugins.documentum.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentUtils {
    private static final int THREADS = 10;

    private static ExecutorService pool = null;

    private ConcurrentUtils() {
    }

    public static void init() {
        pool = Executors.newFixedThreadPool(THREADS);
    }

    public static void shutdown() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    public static void execute(final Runnable runnable) {
        pool.execute(runnable);
    }
}
