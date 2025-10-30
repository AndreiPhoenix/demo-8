package com.demo.cache;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private final ConcurrentCache<String, Object> cache;
    private final ScheduledExecutorService cleanupScheduler;

    public CacheManager() {
        this.cache = new ConcurrentCache<>();
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

        // Запускаем периодическую очистку просроченных записей
        this.cleanupScheduler.scheduleAtFixedRate(
                cache::cleanup,
                1, 1, TimeUnit.MINUTES
        );
    }

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public void put(String key, Object value, long ttl, TimeUnit unit) {
        cache.put(key, value, unit.toMillis(ttl));
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = cache.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public boolean remove(String key) {
        return cache.remove(key);
    }

    public void shutdown() {
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}