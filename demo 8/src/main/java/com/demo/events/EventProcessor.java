package com.demo.events;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventProcessor implements Runnable {
    private final BlockingQueue<Event> eventQueue;
    private final EventBus eventBus;
    private final AtomicBoolean running;
    private final Thread processorThread;

    public EventProcessor(EventBus eventBus) {
        this.eventQueue = new LinkedBlockingQueue<>();
        this.eventBus = eventBus;
        this.running = new AtomicBoolean(true);
        this.processorThread = new Thread(this, "EventProcessor");
    }

    public void start() {
        processorThread.start();
    }

    public void stop() {
        running.set(false);
        processorThread.interrupt();
    }

    public void submit(Event event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (running.get() || !eventQueue.isEmpty()) {
            try {
                Event event = eventQueue.take();
                eventBus.publish(event);
            } catch (InterruptedException e) {
                if (!running.get()) {
                    break;
                }
                Thread.currentThread().interrupt();
            }
        }
    }
}