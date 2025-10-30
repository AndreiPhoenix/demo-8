package com.demo.logging;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncLogger {
    private final BlockingQueue<LogEntry> logQueue;
    private final AtomicBoolean running;
    private final Thread loggerThread;

    public AsyncLogger() {
        this.logQueue = new LinkedBlockingQueue<>();
        this.running = new AtomicBoolean(true);
        this.loggerThread = new Thread(this::processLogs, "AsyncLogger");
        this.loggerThread.start();
    }

    public void log(String level, String message, Object... args) {
        String formattedMessage = String.format(message, args);
        LogEntry logEntry = new LogEntry(level, formattedMessage, Thread.currentThread().getName());

        try {
            logQueue.put(logEntry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void info(String message, Object... args) {
        log("INFO", message, args);
    }

    public void error(String message, Object... args) {
        log("ERROR", message, args);
    }

    public void warn(String message, Object... args) {
        log("WARN", message, args);
    }

    public void debug(String message, Object... args) {
        log("DEBUG", message, args);
    }

    private void processLogs() {
        while (running.get() || !logQueue.isEmpty()) {
            try {
                LogEntry logEntry = logQueue.take();
                writeToLog(logEntry);
            } catch (InterruptedException e) {
                if (!running.get()) {
                    break;
                }
                Thread.currentThread().interrupt();
            }
        }
    }

    private void writeToLog(LogEntry logEntry) {
        System.out.printf("[%s] [%s] [%s] %s%n",
                logEntry.timestamp(),
                logEntry.level(),
                logEntry.threadName(),
                logEntry.message());
    }

    public void shutdown() {
        running.set(false);
        loggerThread.interrupt();
        try {
            loggerThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record LogEntry(String level, String message, String threadName, long timestamp) {
        public LogEntry(String level, String message, String threadName) {
            this(level, message, threadName, System.currentTimeMillis());
        }
    }
}