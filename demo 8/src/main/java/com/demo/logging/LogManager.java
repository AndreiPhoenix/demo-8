package com.demo.logging;

public class LogManager {
    private static final AsyncLogger logger = new AsyncLogger();

    public static AsyncLogger getLogger() {
        return logger;
    }

    public static void shutdown() {
        logger.shutdown();
    }
}