package com.aayadi.threaded;

import java.util.concurrent.atomic.AtomicLong;

public final class SchedulerMetrics {

    private final AtomicLong submitted = new AtomicLong();
    private final AtomicLong completed = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong retried = new AtomicLong();
    private final AtomicLong totalExecutionTime = new AtomicLong();

    public void incrementSubmitted() {
        submitted.incrementAndGet();
    }

    public void incrementCompleted(long execTimeMillis) {
        completed.incrementAndGet();
        totalExecutionTime.addAndGet(execTimeMillis);
    }

    public void incrementFailed(long execTimeMillis) {
        failed.incrementAndGet();
        totalExecutionTime.addAndGet(execTimeMillis);
    }

    public void incrementRetried() {
        retried.incrementAndGet();
    }

    public long getSubmitted() {
        return submitted.get();
    }

    public long getCompleted() {
        return completed.get();
    }

    public long getFailed() {
        return failed.get();
    }

    public long getRetried() {
        return retried.get();
    }

    public double getAverageExecutionTimeMillis() {
        long totalJobs = completed.get() + failed.get();
        return totalJobs == 0
                ? 0
                : (double) totalExecutionTime.get() / totalJobs;
    }
}
