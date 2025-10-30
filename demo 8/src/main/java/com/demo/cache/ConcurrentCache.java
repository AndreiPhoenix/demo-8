package com.demo.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ConcurrentCache<K, V> {
    private final ConcurrentMap<K, CacheEntry<V>> cache;
    private final long defaultTtl; // в миллисекундах

    public ConcurrentCache() {
        this(TimeUnit.MINUTES.toMillis(30)); // TTL по умолчанию 30 минут
    }

    public ConcurrentCache(long defaultTtl) {
        this.cache = new ConcurrentHashMap<>();
        this.defaultTtl = defaultTtl;
    }

    public void put(K key, V value) {
        put(key, value, defaultTtl);
    }

    public void put(K key, V value, long ttl) {
        long expiryTime = System.currentTimeMillis() + ttl;
        cache.put(key, new CacheEntry<>(value, expiryTime));
    }

    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }

        return entry.getValue();
    }

    public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
        return computeIfAbsent(key, mappingFunction, defaultTtl);
    }

    public V computeIfAbsent(K key, Function<K, V> mappingFunction, long ttl) {
        return cache.compute(key, (k, existingEntry) -> {
            if (existingEntry != null && !existingEntry.isExpired()) {
                return existingEntry;
            }

            V newValue = mappingFunction.apply(k);
            long expiryTime = System.currentTimeMillis() + ttl;
            return new CacheEntry<>(newValue, expiryTime);
        }).getValue();
    }

    public boolean remove(K key) {
        return cache.remove(key) != null;
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public void cleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private static class CacheEntry<V> {
        private final V value;
        private final long expiryTime;

        CacheEntry(V value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        V getValue() {
            return value;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}