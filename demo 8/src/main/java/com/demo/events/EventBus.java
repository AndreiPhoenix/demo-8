package com.demo.events;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EventBus {
    private final ConcurrentMap<EventType, CopyOnWriteArrayList<Consumer<Event>>> subscribers;
    private final ExecutorService executor;

    public EventBus() {
        this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    public EventBus(ExecutorService executor) {
        this.subscribers = new ConcurrentHashMap<>();
        this.executor = executor;
    }

    public void subscribe(EventType eventType, Consumer<Event> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    public void unsubscribe(EventType eventType, Consumer<Event> handler) {
        CopyOnWriteArrayList<Consumer<Event>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    public void publish(Event event) {
        CopyOnWriteArrayList<Consumer<Event>> handlers = subscribers.get(event.getType());
        if (handlers != null && !handlers.isEmpty()) {
            for (Consumer<Event> handler : handlers) {
                executor.execute(() -> {
                    try {
                        handler.accept(event);
                    } catch (Exception e) {
                        System.err.println("Error handling event: " + e.getMessage());
                    }
                });
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}