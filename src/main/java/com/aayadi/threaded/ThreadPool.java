package com.aayadi.threaded;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class ThreadPool {
    private final List<Worker> workers;
    private final JobScheduler scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ThreadPool(int size, BlockingQueue<Job> queue, JobScheduler scheduler, Duration timeout) {
        this.scheduler = scheduler;
        this.workers = IntStream.range(0, size)
                .mapToObj(i -> new Worker(queue, scheduler, timeout))
                .toList();
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            workers.forEach(Thread::start);
        }
    }

    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            workers.forEach(Thread::interrupt);
        }
    }
}
