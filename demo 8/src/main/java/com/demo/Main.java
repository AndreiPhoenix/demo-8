package com.demo;

import com.demo.cache.CacheManager;
import com.demo.events.*;
import com.demo.logging.LogManager;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Multi-threaded Caching and Event System...");

        // Инициализация компонентов
        CacheManager cacheManager = new CacheManager();
        EventBus eventBus = new EventBus(Executors.newFixedThreadPool(4));
        EventProcessor eventProcessor = new EventProcessor(eventBus);

        // Настройка подписчиков на события
        setupEventSubscribers(eventBus);

        // Запуск процессора событий
        eventProcessor.start();

        // Демонстрация работы системы
        demonstrateSystem(cacheManager, eventProcessor);

        // Остановка системы
        shutdownSystem(cacheManager, eventBus, eventProcessor);
    }

    private static void setupEventSubscribers(EventBus eventBus) {
        // Подписчик для событий пользователя
        eventBus.subscribe(EventType.USER_LOGIN, event -> {
            LogManager.getLogger().info("User logged in: {}", event.getData("username"));
            // Здесь можно добавить кэширование информации о сессии
        });

        eventBus.subscribe(EventType.USER_LOGOUT, event -> {
            LogManager.getLogger().info("User logged out: {}", event.getData("username"));
            // Здесь можно добавить удаление из кэша
        });

        // Подписчик для событий заказов
        eventBus.subscribe(EventType.ORDER_CREATED, event -> {
            LogManager.getLogger().info("Order created: {} with amount: {}",
                    event.getData("orderId"), event.getData("amount"));
        });

        // Подписчик для системных ошибок
        eventBus.subscribe(EventType.SYSTEM_ERROR, event -> {
            LogManager.getLogger().error("System error: {} from {}",
                    event.getData("error"), event.getSource());
        });
    }

    private static void demonstrateSystem(CacheManager cacheManager, EventProcessor eventProcessor) {
        // Демонстрация кэширования
        demonstrateCaching(cacheManager);

        // Демонстрация системы событий
        demonstrateEventSystem(eventProcessor);

        // Даем время на обработку асинхронных операций
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void demonstrateCaching(CacheManager cacheManager) {
        LogManager.getLogger().info("=== Caching Demonstration ===");

        // Кэширование данных
        cacheManager.put("user:123", "John Doe", 2, TimeUnit.MINUTES);
        cacheManager.put("config:timeout", 5000, 10, TimeUnit.MINUTES);

        // Получение данных из кэша
        String user = cacheManager.get("user:123", String.class);
        Integer timeout = cacheManager.get("config:timeout", Integer.class);

        LogManager.getLogger().info("Cached user: {}", user);
        LogManager.getLogger().info("Cached timeout: {}", timeout);

        // Используем метод getCache() если он добавлен в CacheManager
        // Или альтернативный подход:
        String computedValue = (String) cacheManager.get("computed:key", String.class);
        if (computedValue == null) {
            cacheManager.put("computed:key", "Computed Value", 5, TimeUnit.MINUTES);
            computedValue = "Computed Value";
            LogManager.getLogger().info("Value computed and cached: {}", computedValue);
        } else {
            LogManager.getLogger().info("Retrieved from cache: {}", computedValue);
        }
    }

    private static void demonstrateEventSystem(EventProcessor eventProcessor) {
        LogManager.getLogger().info("=== Event System Demonstration ===");

        // Генерация различных событий
        Event loginEvent = new Event(EventType.USER_LOGIN, "AuthService");
        loginEvent.putData("username", "john_doe");
        loginEvent.putData("ip", "192.168.1.100");

        Event orderEvent = new Event(EventType.ORDER_CREATED, "OrderService");
        orderEvent.putData("orderId", "ORD-12345");
        orderEvent.putData("amount", 99.99);
        orderEvent.putData("currency", "USD");

        Event errorEvent = new Event(EventType.SYSTEM_ERROR, "DatabaseService");
        errorEvent.putData("error", "Connection timeout");
        errorEvent.putData("retryCount", 3);

        // Отправка событий
        eventProcessor.submit(loginEvent);
        eventProcessor.submit(orderEvent);
        eventProcessor.submit(errorEvent);

        LogManager.getLogger().info("Events submitted for processing");
    }

    private static void shutdownSystem(CacheManager cacheManager, EventBus eventBus,
                                       EventProcessor eventProcessor) {
        LogManager.getLogger().info("Shutting down system...");

        eventProcessor.stop();
        eventBus.shutdown();
        cacheManager.shutdown();
        LogManager.shutdown();

        System.out.println("System shutdown completed.");
    }
}