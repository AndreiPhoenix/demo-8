package com.demo.events;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Event {
    private final EventType type;
    private final String source;
    private final LocalDateTime timestamp;
    private final Map<String, Object> data;

    public Event(EventType type, String source) {
        this(type, source, new ConcurrentHashMap<>());
    }

    public Event(EventType type, String source, Map<String, Object> data) {
        this.type = type;
        this.source = source;
        this.timestamp = LocalDateTime.now();
        this.data = new ConcurrentHashMap<>(data);
    }

    // Геттеры
    public EventType getType() { return type; }
    public String getSource() { return source; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Object> getData() { return data; }

    public void putData(String key, Object value) {
        data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    @Override
    public String toString() {
        return String.format("Event{type=%s, source='%s', timestamp=%s, data=%s}",
                type, source, timestamp, data);
    }
}